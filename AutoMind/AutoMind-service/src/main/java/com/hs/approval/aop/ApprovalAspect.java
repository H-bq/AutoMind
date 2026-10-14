package com.hs.approval.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.agent.core.AgentContext;
import com.hs.approval.ApprovalService;
import com.hs.approval.ApprovalStatus;
import com.hs.approval.ApprovalType;
import com.hs.approval.RiskLevel;
import com.hs.approval.exception.ApprovalPendingException;
import com.hs.approval.exception.ApprovalRejectedException;
import com.hs.domain.po.ApprovalRecord;
import com.hs.domain.po.McpCallLog;
import com.hs.mcp.McpCallContext;
import com.hs.mcp.McpToolRegistry;
import com.hs.service.IMcpCallLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class ApprovalAspect {

    private final ApprovalService approvalService;
    private final McpToolRegistry toolRegistry;
    private final IMcpCallLogService mcpCallLogService;
    private final ObjectMapper objectMapper;

    @Around("execution(public * com.hs.mcp.McpClient.invoke(..))")
    public Object aroundMcpInvoke(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        McpCallContext ctx = findContext(args);
        if (ctx == null || !toolRegistry.isHighRisk(ctx.toolName())) {
            return joinPoint.proceed();
        }
        ApprovalRecord approved = approvalService.findLatestForTask(ctx.agent().agentTaskId(), ApprovalType.HIGH_RISK_MCP)
                .or(() -> approvalService.findApprovedForIntent(ctx.agent().intentId(), ApprovalType.HIGH_RISK_MCP))
                .orElse(null);
        if (approved != null) {
            if (ApprovalStatus.APPROVED.name().equals(approved.getStatus())) {
                return joinPoint.proceed();
            }
            if (ApprovalStatus.PENDING.name().equals(approved.getStatus())) {
                throw pending(approved, ctx);
            }
            throw new ApprovalRejectedException(approved.getId(), ApprovalType.HIGH_RISK_MCP,
                    approved.getDecisionReason(), ctx.agent().traceId());
        }

        Long approvalId = approvalService.submit(ctx.agent(), ApprovalType.HIGH_RISK_MCP, RiskLevel.HIGH,
                Map.of("toolName", ctx.toolName(), "toolVersion", ctx.toolVersion()),
                args.length > 1 ? args[1] : Map.of());
        savePermissionLog(ctx, args.length > 1 ? args[1] : null, approvalId);
        throw new ApprovalPendingException(approvalId, ApprovalType.HIGH_RISK_MCP,
                ctx.agent().intentId(), ctx.agent().agentTaskId(), ctx.agent().traceId());
    }

    @Around("@annotation(requireApproval)")
    public Object aroundAnnotated(ProceedingJoinPoint joinPoint, RequireApproval requireApproval) throws Throwable {
        AgentContext ctx = findAgentContext(joinPoint.getArgs());
        if (ctx == null) {
            return joinPoint.proceed();
        }
        ApprovalType type = requireApproval.type();
        ApprovalRecord latest = approvalService.findLatestForTask(ctx.agentTaskId(), type)
                .or(() -> approvalService.findApprovedForIntent(ctx.intentId(), type))
                .orElse(null);
        if (latest != null) {
            if (ApprovalStatus.APPROVED.name().equals(latest.getStatus())) {
                return joinPoint.proceed();
            }
            if (ApprovalStatus.PENDING.name().equals(latest.getStatus())) {
                throw new ApprovalPendingException(latest.getId(), type, ctx.intentId(), ctx.agentTaskId(), ctx.traceId());
            }
            throw new ApprovalRejectedException(latest.getId(), type, latest.getDecisionReason(), ctx.traceId());
        }
        Long approvalId = approvalService.submit(ctx, type, RiskLevel.HIGH,
                Map.of("method", joinPoint.getSignature().toShortString()), joinPoint.getArgs());
        throw new ApprovalPendingException(approvalId, type, ctx.intentId(), ctx.agentTaskId(), ctx.traceId());
    }

    private AgentContext findAgentContext(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof AgentContext context) {
                return context;
            }
            if (arg instanceof McpCallContext context) {
                return context.agent();
            }
        }
        return null;
    }

    private McpCallContext findContext(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof McpCallContext context) {
                return context;
            }
        }
        return null;
    }

    private ApprovalPendingException pending(ApprovalRecord record, McpCallContext ctx) {
        return new ApprovalPendingException(record.getId(), ApprovalType.HIGH_RISK_MCP,
                ctx.agent().intentId(), ctx.agent().agentTaskId(), ctx.agent().traceId());
    }

    private void savePermissionLog(McpCallContext ctx, Object params, Long approvalId) {
        mcpCallLogService.save(new McpCallLog()
                .setTenantId(ctx.agent().tenantId())
                .setAgentTaskId(ctx.agent().agentTaskId())
                .setIntentId(ctx.agent().intentId())
                .setTraceId(ctx.agent().traceId())
                .setToolName(ctx.toolName())
                .setToolVersion(ctx.toolVersion())
                .setRequestParams(toJson(params == null ? Map.of() : params))
                .setResponseCode(412)
                .setResponseBody(toJson(Map.of("approvalId", approvalId, "message", "awaiting approval")))
                .setErrorType("PERMISSION")
                .setLatencyMs(0)
                .setAttemptNo(ctx.attemptNo())
                .setIsHighRisk(true)
                .setCalledAt(LocalDateTime.now()));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{\"serializationError\":\"" + e.getMessage() + "\"}";
        }
    }
}
