package com.hs.approval.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.agent.core.AgentContext;
import com.hs.approval.ApprovalService;
import com.hs.approval.ApprovalStatus;
import com.hs.approval.ApprovalType;
import com.hs.approval.exception.ApprovalPendingException;
import com.hs.approval.exception.ApprovalRejectedException;
import com.hs.domain.po.ApprovalRecord;
import com.hs.mcp.McpCallContext;
import com.hs.mcp.McpResponse;
import com.hs.mcp.McpToolRegistry;
import com.hs.service.IMcpCallLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ApprovalAspectTest {

    private final ApprovalService approvalService = mock(ApprovalService.class);
    private final McpToolRegistry toolRegistry = mock(McpToolRegistry.class);
    private final IMcpCallLogService mcpCallLogService = mock(IMcpCallLogService.class);
    private final ApprovalAspect aspect = new ApprovalAspect(approvalService, toolRegistry, mcpCallLogService, new ObjectMapper());

    @Test
    void nonHighRiskToolProceedsWithoutApprovalLookup() throws Throwable {
        McpCallContext ctx = context("get_topo");
        ProceedingJoinPoint joinPoint = joinPoint(ctx, Map.of(), "ok");
        when(toolRegistry.isHighRisk("get_topo")).thenReturn(false);

        Object result = aspect.aroundMcpInvoke(joinPoint);

        assertEquals("ok", result);
        verify(joinPoint).proceed();
        verifyNoInteractions(approvalService);
    }

    @Test
    void highRiskToolCreatesPendingApprovalAndBlocks() throws Throwable {
        McpCallContext ctx = context("set_qos");
        ProceedingJoinPoint joinPoint = joinPoint(ctx, Map.of("bandwidth", 100), null);
        when(toolRegistry.isHighRisk("set_qos")).thenReturn(true);
        when(approvalService.findLatestForTask(10L, ApprovalType.HIGH_RISK_MCP)).thenReturn(Optional.empty());
        when(approvalService.findApprovedForIntent(7, ApprovalType.HIGH_RISK_MCP)).thenReturn(Optional.empty());
        when(approvalService.submit(any(), eq(ApprovalType.HIGH_RISK_MCP), any(), any(), any())).thenReturn(99L);

        ApprovalPendingException ex = assertThrows(ApprovalPendingException.class, () -> aspect.aroundMcpInvoke(joinPoint));

        assertEquals(99L, ex.getApprovalId());
        verify(mcpCallLogService).save(any());
    }

    @Test
    void approvedHighRiskToolProceeds() throws Throwable {
        McpCallContext ctx = context("set_qos");
        ProceedingJoinPoint joinPoint = joinPoint(ctx, Map.of(), new McpResponse(200, null, Map.of(), 1));
        when(toolRegistry.isHighRisk("set_qos")).thenReturn(true);
        when(approvalService.findLatestForTask(10L, ApprovalType.HIGH_RISK_MCP))
                .thenReturn(Optional.of(record(ApprovalStatus.APPROVED)));

        Object result = aspect.aroundMcpInvoke(joinPoint);

        assertInstanceOf(McpResponse.class, result);
        verify(joinPoint).proceed();
    }

    @Test
    void rejectedHighRiskToolThrowsRejected() throws Throwable {
        McpCallContext ctx = context("set_qos");
        ProceedingJoinPoint joinPoint = joinPoint(ctx, Map.of(), null);
        when(toolRegistry.isHighRisk("set_qos")).thenReturn(true);
        when(approvalService.findLatestForTask(10L, ApprovalType.HIGH_RISK_MCP))
                .thenReturn(Optional.of(record(ApprovalStatus.REJECTED)));

        assertThrows(ApprovalRejectedException.class, () -> aspect.aroundMcpInvoke(joinPoint));
    }

    private McpCallContext context(String toolName) {
        AgentContext agent = new AgentContext(10L, null, 7, "tenant-001", "OPERATOR", 1, "trace-1", "EXECUTOR");
        return McpCallContext.of(agent, toolName, "v1");
    }

    private ProceedingJoinPoint joinPoint(McpCallContext ctx, Map<String, Object> params, Object result) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{ctx, params});
        when(joinPoint.proceed()).thenReturn(result);
        return joinPoint;
    }

    private ApprovalRecord record(ApprovalStatus status) {
        return new ApprovalRecord()
                .setId(11L)
                .setStatus(status.name())
                .setDecisionReason("reason");
    }
}
