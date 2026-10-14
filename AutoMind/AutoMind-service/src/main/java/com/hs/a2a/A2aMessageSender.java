package com.hs.a2a;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.a2a.config.AutomindA2aProperties;
import com.hs.a2a.event.A2aDegradedEvent;
import com.hs.domain.po.A2aMessagePO;
import com.hs.domain.po.NetworkDomain;
import com.hs.service.IA2aMessageService;
import com.hs.service.INetworkDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class A2aMessageSender {

    private final AutomindA2aProperties properties;
    private final A2aPayloadSanitizer sanitizer;
    private final IA2aMessageService a2aMessageService;
    private final INetworkDomainService networkDomainService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Executor a2aExecutor;

    public A2aMessageSender(AutomindA2aProperties properties,
                            A2aPayloadSanitizer sanitizer,
                            IA2aMessageService a2aMessageService,
                            INetworkDomainService networkDomainService,
                            ObjectMapper objectMapper,
                            ApplicationEventPublisher eventPublisher,
                            @Qualifier("a2aExecutor") Executor a2aExecutor) {
        this.properties = properties;
        this.sanitizer = sanitizer;
        this.a2aMessageService = a2aMessageService;
        this.networkDomainService = networkDomainService;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.a2aExecutor = a2aExecutor;
    }

    public Optional<A2aMessage> sendAndAwait(A2aMessage req, Duration timeout) {
        A2aMessage sanitizedReq = req.withPayload(sanitizer.sanitize(req.payload()));
        long start = System.nanoTime();
        saveMessage(sanitizedReq, "REQ", null, LocalDateTime.now(), null);
        CompletableFuture<A2aMessage> future = CompletableFuture.supplyAsync(() -> post(sanitizedReq), a2aExecutor);
        long timeoutMs = timeout == null ? properties.getTimeoutMs() : timeout.toMillis();
        try {
            A2aMessage response = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            saveMessage(response, "RESP", elapsedMs(start), null, LocalDateTime.now());
            return Optional.of(response);
        } catch (Exception e) {
            log.warn("A2A send timeout/fail targetDomain={}, traceId={}", req.targetDomainId(), req.traceId());
            A2aMessage timeoutMessage = sanitizedReq.withType(A2aMessageType.TIMEOUT);
            saveMessage(timeoutMessage, "TIMEOUT", elapsedMs(start), null, LocalDateTime.now());
            eventPublisher.publishEvent(new A2aDegradedEvent(this, timeoutMessage));
            return Optional.empty();
        }
    }

    private A2aMessage post(A2aMessage req) {
        String endpoint = resolveEndpoint(req.targetDomainId());
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getTimeoutMs()));
        Map<String, Object> response = RestClient.builder()
                .baseUrl(endpoint)
                .requestFactory(requestFactory)
                .build()
                .post()
                .uri(properties.getInboxPath())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Trace-Id", req.traceId())
                .header("X-Tenant-Id", req.tenantId())
                .header("X-Role", "PLANNER")
                .body(req)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                });
        Map<String, Object> payload = response == null ? Map.of() : response;
        return new A2aMessage(req.tenantId(), req.targetDomainId(), req.sourceDomainId(), req.targetAgent(),
                req.sourceAgent(), req.taskId(), req.traceId(), A2aMessageType.RESP, sanitizer.sanitize(payload));
    }

    private String resolveEndpoint(String targetDomainId) {
        if (targetDomainId != null && !targetDomainId.isBlank()) {
            NetworkDomain domain = networkDomainService.getById(targetDomainId);
            if (domain != null && domain.getControllerEndpoint() != null && !domain.getControllerEndpoint().isBlank()) {
                return trimTrailingSlash(domain.getControllerEndpoint());
            }
        }
        return "http://localhost:9090";
    }

    public void saveMessage(A2aMessage message, String status, Integer latencyMs, LocalDateTime sentAt, LocalDateTime receivedAt) {
        a2aMessageService.save(new A2aMessagePO()
                .setTenantId(message.tenantId())
                .setSourceDomainId(message.sourceDomainId())
                .setTargetDomainId(message.targetDomainId())
                .setSourceAgent(message.sourceAgent())
                .setTargetAgent(message.targetAgent())
                .setTaskId(message.taskId())
                .setTraceId(message.traceId())
                .setMessageType(message.type().name())
                .setPayload(toJson(message.payload()))
                .setStatus(status)
                .setLatencyMs(latencyMs)
                .setSentAt(sentAt)
                .setReceivedAt(receivedAt));
    }

    private int elapsedMs(long start) {
        return (int) Math.min(Integer.MAX_VALUE, Duration.ofNanos(System.nanoTime() - start).toMillis());
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
