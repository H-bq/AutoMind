package com.hs.agent.core;

import com.hs.common.exception.ForbiddenException;
import com.hs.common.trace.TraceIdHolder;
import com.hs.common.utils.UserContext;

public record AgentContext(
        Long agentTaskId,
        Long parentTaskId,
        Integer intentId,
        String tenantId,
        String role,
        Integer userId,
        String traceId,
        String agentRole
) {

    public static AgentContext bootstrap(Integer intentId, String agentRole) {
        String tenantId = UserContext.requireTenantId();
        String role = UserContext.requireRole();
        String traceId = TraceIdHolder.get();
        if (traceId == null || traceId.isBlank()) {
            throw new ForbiddenException("缺少 TraceId 上下文");
        }
        return new AgentContext(null, null, intentId, tenantId, role, UserContext.getUser(), traceId, agentRole);
    }

    public AgentContext withTaskId(Long taskId) {
        return new AgentContext(taskId, parentTaskId, intentId, tenantId, role, userId, traceId, agentRole);
    }

    public AgentContext childOf(Long parentTaskId, String newRole) {
        return new AgentContext(null, parentTaskId, intentId, tenantId, role, userId, traceId, newRole);
    }
}
