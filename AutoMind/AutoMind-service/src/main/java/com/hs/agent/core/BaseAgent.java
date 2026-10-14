package com.hs.agent.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.agent.exception.LowConfidenceException;
import com.hs.common.exception.ForbiddenException;
import com.hs.domain.po.AgentTask;
import com.hs.domain.po.IntentRequest;
import com.hs.mapper.AgentTaskMapper;
import com.hs.mcp.exception.McpException;
import com.hs.service.IIntentRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseAgent<I, O> {

    private final AgentTaskMapper agentTaskMapper;
    private final IIntentRequestService intentRequestService;
    private final ObjectMapper objectMapper;

    protected abstract String agentRole();

    protected abstract String taskType();

    protected abstract String initialPevState();

    protected abstract O doExecute(AgentContext ctx, I input);

    public final O execute(AgentContext ctx, I input) {
        return executeWithContext(ctx, input).output();
    }

    public final AgentRunResult<O> executeWithContext(AgentContext ctx, I input) {
        validateTenant(ctx);
        AgentTask task = new AgentTask()
                .setTenantId(ctx.tenantId())
                .setIntentId(ctx.intentId())
                .setParentTaskId(ctx.parentTaskId())
                .setTraceId(ctx.traceId())
                .setAgentRole(agentRole())
                .setTaskType(taskType())
                .setPevState(initialPevState())
                .setRetryCount(0)
                .setInputPayload(toJson(input))
                .setStartedAt(LocalDateTime.now());
        agentTaskMapper.insert(task);

        AgentContext taskContext = ctx.withTaskId(task.getId());
        try {
            log.info("Agent task started role={}, taskId={}, intentId={}", agentRole(), task.getId(), ctx.intentId());
            O output = doExecute(taskContext, input);
            task.setPevState("COMPLETED")
                    .setOutputPayload(toJson(output))
                    .setConfidence(output instanceof Confident confident ? confident.getConfidence() : null)
                    .setFinishedAt(LocalDateTime.now());
            agentTaskMapper.updateById(task);
            log.info("Agent task completed role={}, taskId={}", agentRole(), task.getId());
            return new AgentRunResult<>(taskContext, output);
        } catch (LowConfidenceException e) {
            markFailed(task, "LOW_CONFIDENCE", e.getOriginalPayload());
            throw e;
        } catch (Throwable e) {
            markFailed(task, errorCode(e), e.getMessage());
            throw e;
        }
    }

    private void validateTenant(AgentContext ctx) {
        IntentRequest intentRequest = intentRequestService.getById(ctx.intentId());
        if (intentRequest == null) {
            throw new ForbiddenException("意图请求不存在或无权访问");
        }
        if (!ctx.tenantId().equals(intentRequest.getTenantId())) {
            throw new ForbiddenException("Agent 任务租户上下文不一致");
        }
    }

    private void markFailed(AgentTask task, String errorCode, String payload) {
        task.setPevState("FAILED")
                .setErrorCode(truncate(errorCode, 64))
                .setOutputPayload(toJson(payload))
                .setFinishedAt(LocalDateTime.now());
        agentTaskMapper.updateById(task);
        log.error("Agent task failed role={}, taskId={}, errorCode={}", task.getAgentRole(), task.getId(), errorCode);
    }

    private String errorCode(Throwable e) {
        if (e instanceof McpException mcpException) {
            return "MCP_" + mcpException.getErrorType().name();
        }
        String message = e.getMessage() == null ? "" : ":" + e.getMessage();
        return e.getClass().getSimpleName() + message;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{\"serializationError\":\"" + truncate(e.getMessage(), 120) + "\"}";
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
