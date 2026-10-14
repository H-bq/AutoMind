package com.hs.a2a;

import java.util.Map;

public record A2aMessage(
        String tenantId,
        String sourceDomainId,
        String targetDomainId,
        String sourceAgent,
        String targetAgent,
        Long taskId,
        String traceId,
        A2aMessageType type,
        Map<String, Object> payload
) {
    public A2aMessage withType(A2aMessageType newType) {
        return new A2aMessage(tenantId, sourceDomainId, targetDomainId, sourceAgent, targetAgent, taskId, traceId, newType, payload);
    }

    public A2aMessage withPayload(Map<String, Object> newPayload) {
        return new A2aMessage(tenantId, sourceDomainId, targetDomainId, sourceAgent, targetAgent, taskId, traceId, type, newPayload);
    }
}
