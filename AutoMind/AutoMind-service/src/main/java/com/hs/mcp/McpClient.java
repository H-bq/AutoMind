package com.hs.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.common.trace.TraceIdHolder;
import com.hs.domain.po.McpCallLog;
import com.hs.mcp.config.AutomindMcpProperties;
import com.hs.mcp.event.McpTimeoutEvent;
import com.hs.mcp.exception.McpException;
import com.hs.service.IMcpCallLogService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpClient {

    private final AutomindMcpProperties properties;
    private final McpToolRegistry toolRegistry;
    private final McpErrorClassifier errorClassifier;
    private final IMcpCallLogService mcpCallLogService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public McpResponse invoke(McpCallContext ctx, Map<String, Object> params) {
        AtomicInteger attempt = new AtomicInteger(Math.max(1, ctx.attemptNo()) - 1);
        Supplier<McpResponse> supplier = () -> invokeOnce(ctx.withAttemptNo(attempt.incrementAndGet()), params);
        Retry retry = Retry.of("mcpRetryRuntime", RetryConfig.custom()
                .maxAttempts(Math.max(1, properties.getMaxRetries()))
                .waitDuration(Duration.ofMillis(Math.max(0, properties.getRetryBackoffMs())))
                .retryOnException(e -> e instanceof McpException mcpException && mcpException.getErrorType().retryable())
                .build());
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("mcpCb");
        return CircuitBreaker.decorateSupplier(circuitBreaker, Retry.decorateSupplier(retry, supplier)).get();
    }

    private McpResponse invokeOnce(McpCallContext ctx, Map<String, Object> params) {
        long start = System.nanoTime();
        McpToolRegistry.McpToolDef toolDef = null;
        McpResponse response = null;
        McpErrorType errorType = null;
        try {
            toolDef = validateTool(ctx);
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofMillis(properties.getTimeoutMs()));
            requestFactory.setReadTimeout(Duration.ofMillis(properties.getTimeoutMs()));
            RestClient restClient = RestClient.builder()
                    .baseUrl(trimTrailingSlash(properties.getServerBaseUrl()))
                    .requestFactory(requestFactory)
                    .build();
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("params", params == null ? Map.of() : params);
            Map<String, Object> contextBody = new LinkedHashMap<>();
            contextBody.put("agentTaskId", ctx.agent().agentTaskId());
            contextBody.put("intentId", ctx.agent().intentId());
            contextBody.put("traceId", ctx.agent().traceId());
            contextBody.put("toolName", ctx.toolName());
            contextBody.put("toolVersion", ctx.toolVersion());
            contextBody.put("attemptNo", ctx.attemptNo());
            contextBody.put("dryRun", ctx.dryRun());
            requestBody.put("context", contextBody);

            response = restClient.post()
                    .uri("/mcp/tools/{name}/invoke", ctx.toolName())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Trace-Id", ctx.agent().traceId())
                    .header("X-Tenant-Id", ctx.agent().tenantId())
                    .header("X-Agent-Task-Id", String.valueOf(ctx.agent().agentTaskId()))
                    .header("X-Role", ctx.agent().role())
                    .header("X-Actor-Id", String.valueOf(ctx.agent().userId()))
                    .body(requestBody)
                    .exchange((request, clientResponse) -> {
                        String body = StreamUtils.copyToString(clientResponse.getBody(), StandardCharsets.UTF_8);
                        int status = clientResponse.getStatusCode().value();
                        McpErrorType classified = errorClassifier.classifyHttp(status, body);
                        return new McpResponse(status, classified, parseBody(body), elapsedMs(start));
                    });
            errorType = response.errorType();
            if (errorType != null) {
                throw new McpException(errorType, ctx, response, "MCP 调用失败：" + errorType.name());
            }
            return response;
        } catch (McpException e) {
            errorType = e.getErrorType();
            if (errorType == McpErrorType.TIMEOUT) {
                eventPublisher.publishEvent(new McpTimeoutEvent(this, ctx));
            }
            throw e;
        } catch (Throwable e) {
            errorType = errorClassifier.classifyException(e);
            response = new McpResponse(502, errorType, Map.of("message", e.getMessage() == null ? "" : e.getMessage()), elapsedMs(start));
            if (errorType == McpErrorType.TIMEOUT) {
                eventPublisher.publishEvent(new McpTimeoutEvent(this, ctx));
            }
            throw new McpException(errorType, ctx, response, "MCP 调用异常：" + errorType.name(), e);
        } finally {
            saveLog(ctx, params, toolDef, response, errorType, elapsedMs(start));
        }
    }

    private McpToolRegistry.McpToolDef validateTool(McpCallContext ctx) {
        McpToolRegistry.McpToolDef toolDef = toolRegistry.find(ctx.toolName())
                .orElseThrow(() -> new McpException(McpErrorType.TOOL_NOT_FOUND, ctx,
                        new McpResponse(404, McpErrorType.TOOL_NOT_FOUND, Map.of("message", "tool not registered"), 0),
                        "MCP 工具未注册：" + ctx.toolName()));
        Set<String> requiredRoles = toolDef.requiredRoles();
        String role = ctx.agent().role() == null ? "" : ctx.agent().role().toUpperCase(Locale.ROOT);
        if (!requiredRoles.isEmpty() && !requiredRoles.contains(role)) {
            throw new McpException(McpErrorType.PERMISSION, ctx,
                    new McpResponse(403, McpErrorType.PERMISSION, Map.of("message", "role denied"), 0),
                    "当前角色无权调用 MCP 工具：" + ctx.toolName());
        }
        return toolDef;
    }

    private void saveLog(McpCallContext ctx,
                         Map<String, Object> params,
                         McpToolRegistry.McpToolDef toolDef,
                         McpResponse response,
                         McpErrorType errorType,
                         long latencyMs) {
        try {
            mcpCallLogService.save(new McpCallLog()
                    .setTenantId(ctx.agent().tenantId())
                    .setAgentTaskId(ctx.agent().agentTaskId())
                    .setIntentId(ctx.agent().intentId())
                    .setTraceId(ctx.agent().traceId() == null ? TraceIdHolder.get() : ctx.agent().traceId())
                    .setToolName(ctx.toolName())
                    .setToolVersion(ctx.toolVersion())
                    .setRequestParams(toJson(params == null ? Map.of() : params))
                    .setResponseCode(response == null ? null : response.httpStatus())
                    .setResponseBody(response == null ? null : toJson(response.body()))
                    .setErrorType(errorType == null ? null : errorType.name())
                    .setLatencyMs((int) Math.min(Integer.MAX_VALUE, latencyMs))
                    .setAttemptNo(ctx.attemptNo())
                    .setIsHighRisk(toolDef != null && toolDef.highRisk())
                    .setCalledAt(LocalDateTime.now()));
        } catch (Exception logException) {
            log.error("写入 MCP 调用审计失败", logException);
        }
    }

    private Object parseBody(String body) {
        if (body == null || body.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ignored) {
            return Map.of("raw", body);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{\"serializationError\":\"" + e.getMessage() + "\"}";
        }
    }

    private long elapsedMs(long start) {
        return Duration.ofNanos(System.nanoTime() - start).toMillis();
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:9090";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
