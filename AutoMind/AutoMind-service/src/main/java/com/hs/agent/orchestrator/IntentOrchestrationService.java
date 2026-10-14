package com.hs.agent.orchestrator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.agent.core.AgentContext;
import com.hs.agent.core.AgentRunResult;
import com.hs.agent.dto.ExecutionResult;
import com.hs.agent.dto.ParsedIntent;
import com.hs.agent.dto.PlanDecision;
import com.hs.agent.dto.SlaVerdict;
import com.hs.agent.exception.LowConfidenceException;
import com.hs.agent.impl.ExecutorAgent;
import com.hs.agent.impl.MonitorAgent;
import com.hs.agent.impl.PlannerAgent;
import com.hs.agent.impl.SupervisorAgent;
import com.hs.approval.ApprovalService;
import com.hs.approval.ApprovalType;
import com.hs.approval.RiskLevel;
import com.hs.approval.exception.ApprovalPendingException;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.AuditLog;
import com.hs.domain.po.AgentTask;
import com.hs.domain.po.IntentRequest;
import com.hs.domain.po.User;
import com.hs.healing.event.SlaBreachEvent;
import com.hs.service.IAgentTaskService;
import com.hs.service.IAuditLogService;
import com.hs.service.IIntentRequestService;
import com.hs.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntentOrchestrationService {

    private final IIntentRequestService intentRequestService;
    private final IAgentTaskService agentTaskService;
    private final IAuditLogService auditLogService;
    private final IUserService userService;
    private final ApprovalService approvalService;
    private final SupervisorAgent supervisorAgent;
    private final PlannerAgent plannerAgent;
    private final ExecutorAgent executorAgent;
    private final MonitorAgent monitorAgent;
    private final PevStateMachine pevStateMachine;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${automind.agent.max-retries:3}")
    private Integer maxRetries;

    @Async("agentExecutor")
    public void kickoff(Integer intentId) {
        try {
            IntentRequest intentRequest = intentRequestService.getById(intentId);
            if (intentRequest == null) {
                log.error("Intent not found intentId={}", intentId);
                return;
            }
            restoreIntentContext(intentRequest);
            AgentRunResult<ParsedIntent> supervisor = supervisorAgent.executeWithContext(
                    AgentContext.bootstrap(intentId, "SUPERVISOR"), intentRequest.getInputText());
            ParsedIntent parsedIntent = supervisor.output();
            intentRequestService.lambdaUpdate()
                    .eq(IntentRequest::getId, intentId)
                    .set(IntentRequest::getParsedIntent, toJson(parsedIntent))
                    .set(IntentRequest::getStatus, "SUPERVISED")
                    .update();
            if (parsedIntent.getMissingFields() != null && !parsedIntent.getMissingFields().isEmpty()) {
                approvalService.submit(supervisor.context(), ApprovalType.SUPPLEMENT, RiskLevel.LOW, parsedIntent, intentRequest.getInputText());
                markNeedsSupplement(intentId, parsedIntent.getMissingFields());
                return;
            }

            AgentRunResult<PlanDecision> plan = plannerAgent.executeWithContext(supervisor.context().childOf(
                    supervisor.context().agentTaskId(), "PLANNER"), parsedIntent);
            PlanDecision planDecision = plan.output();
            if (requiresPlanApproval(planDecision) && approvalService.findApprovedForIntent(intentId, ApprovalType.PLAN_DECISION).isEmpty()) {
                approvalService.submit(plan.context(), ApprovalType.PLAN_DECISION, toRiskLevel(planDecision.getRiskLevel()), planDecision, parsedIntent);
                markAwaitApproval(intentId, plan.context().agentTaskId(), "PLAN_DECISION");
                return;
            }
            if (Boolean.TRUE.equals(planDecision.getCrossDomainDegraded())
                    && approvalService.findApprovedForIntent(intentId, ApprovalType.CROSS_DOMAIN).isEmpty()) {
                approvalService.submit(plan.context(), ApprovalType.CROSS_DOMAIN, RiskLevel.MEDIUM, planDecision, parsedIntent);
                markAwaitApproval(intentId, plan.context().agentTaskId(), "CROSS_DOMAIN");
                return;
            }

            try {
                AgentRunResult<ExecutionResult> execution = executorAgent.executeWithContext(
                        plan.context().childOf(plan.context().agentTaskId(), "EXECUTOR"), planDecision);
                AgentRunResult<SlaVerdict> monitor = monitorAgent.executeWithContext(
                        execution.context().childOf(execution.context().agentTaskId(), "MONITOR"), execution.output());
                SlaVerdict verdict = monitor.output();
                if (Boolean.TRUE.equals(verdict.getOverallBreach())) {
                    eventPublisher.publishEvent(new SlaBreachEvent(this, monitor.context(), verdict));
                    intentRequestService.lambdaUpdate()
                            .eq(IntentRequest::getId, intentId)
                            .set(IntentRequest::getStatus, "HEALING")
                            .update();
                    return;
                }
                intentRequestService.lambdaUpdate()
                        .eq(IntentRequest::getId, intentId)
                        .set(IntentRequest::getStatus, "COMPLETED")
                        .update();
            } catch (ApprovalPendingException e) {
                markAwaitApproval(intentId, e.getAgentTaskId(), "HIGH_RISK_MCP");
                log.info("Intent awaiting approval intentId={}, approvalId={}", intentId, e.getApprovalId());
            }
        } catch (LowConfidenceException e) {
            AgentTask latest = latestTask(intentId, "SUPERVISOR");
            AgentContext ctx = AgentContext.bootstrap(intentId, "SUPERVISOR")
                    .withTaskId(latest == null ? null : latest.getId());
            Long approvalId = approvalService.submit(ctx, ApprovalType.SUPPLEMENT, RiskLevel.LOW,
                    e.getOriginalPayload(), Map.of("actualConfidence", e.getActualConfidence(), "threshold", e.getThreshold()));
            markNeedsSupplement(intentId, List.of("confidence"));
            log.info("Intent requires supplement intentId={}, approvalId={}, actualConfidence={}", intentId, approvalId, e.getActualConfidence());
        } catch (Exception e) {
            log.error("PEV orchestration failed intentId={}", intentId, e);
            intentRequestService.lambdaUpdate()
                    .eq(IntentRequest::getId, intentId)
                    .set(IntentRequest::getStatus, "FAILED")
                    .update();
        }
    }

    @Async("agentExecutor")
    public void resume(Integer intentId) {
        log.info("Resume PEV orchestration intentId={}", intentId);
        kickoff(intentId);
    }

    @Async("agentExecutor")
    public void replan(Integer intentId) {
        log.info("Replan PEV orchestration intentId={}", intentId);
        intentRequestService.lambdaUpdate()
                .eq(IntentRequest::getId, intentId)
                .set(IntentRequest::getStatus, "REPLANNING")
                .update();
        kickoff(intentId);
    }

    public void rejectForApproval(Integer intentId, String reason) {
        intentRequestService.lambdaUpdate()
                .eq(IntentRequest::getId, intentId)
                .set(IntentRequest::getStatus, "REJECTED")
                .update();
        agentTaskService.lambdaUpdate()
                .eq(AgentTask::getIntentId, intentId)
                .set(AgentTask::getPevState, "FAILED")
                .set(AgentTask::getErrorCode, "APPROVAL_REJECTED")
                .set(AgentTask::getOutputPayload, toJson(Map.of(
                        "status", "REJECTED",
                        "reason", reason == null ? "" : reason
                )))
                .set(AgentTask::getFinishedAt, LocalDateTime.now())
                .update();
    }

    private void markNeedsSupplement(Integer intentId, List<String> missingFields) {
        String payload = toJson(Map.of("status", "NEEDS_SUPPLEMENT", "missingFields", missingFields));
        intentRequestService.lambdaUpdate()
                .eq(IntentRequest::getId, intentId)
                .set(IntentRequest::getParsedIntent, payload)
                .set(IntentRequest::getStatus, "NEEDS_SUPPLEMENT")
                .update();
        auditLogService.save(new AuditLog()
                .setTenantId(UserContext.tenantId())
                .setActorId(UserContext.getUser())
                .setActorRole(UserContext.role())
                .setAction("SUPPLEMENT")
                .setCreatedAt(LocalDateTime.now()));
    }

    private void markAwaitApproval(Integer intentId, Long agentTaskId, String reason) {
        intentRequestService.lambdaUpdate()
                .eq(IntentRequest::getId, intentId)
                .set(IntentRequest::getStatus, "AWAIT_APPROVAL")
                .update();
        if (agentTaskId != null) {
            agentTaskService.lambdaUpdate()
                    .eq(AgentTask::getId, agentTaskId)
                    .set(AgentTask::getPevState, "EXECUTE")
                    .set(AgentTask::getErrorCode, "AWAIT_APPROVAL")
                    .set(AgentTask::getOutputPayload, toJson(Map.of(
                            "status", "AWAIT_APPROVAL",
                            "approvalType", reason == null ? "" : reason
                    )))
                    .set(AgentTask::getFinishedAt, null)
                    .update();
        }
    }

    private boolean requiresPlanApproval(PlanDecision decision) {
        return Boolean.TRUE.equals(decision.getRequiresApproval()) || "HIGH".equalsIgnoreCase(decision.getRiskLevel());
    }

    private RiskLevel toRiskLevel(String value) {
        if ("HIGH".equalsIgnoreCase(value)) {
            return RiskLevel.HIGH;
        }
        if ("LOW".equalsIgnoreCase(value)) {
            return RiskLevel.LOW;
        }
        return RiskLevel.MEDIUM;
    }

    private AgentTask latestTask(Integer intentId, String role) {
        return agentTaskService.lambdaQuery()
                .eq(AgentTask::getIntentId, intentId)
                .eq(AgentTask::getAgentRole, role)
                .orderByDesc(AgentTask::getStartedAt)
                .last("LIMIT 1")
                .one();
    }

    private void restoreIntentContext(IntentRequest intentRequest) {
        Integer userId = intentRequest.getUserId();
        User submitter = userId == null ? null : userService.lambdaQuery()
                .eq(User::getUserId, userId)
                .one();
        String role = agentExecutionRole(submitter);
        UserContext.setUserInfo(userId, intentRequest.getTenantId(), role, intentRequest.getId());
        if (intentRequest.getTraceId() != null && !intentRequest.getTraceId().isBlank()) {
            com.hs.common.trace.TraceIdHolder.set(intentRequest.getTraceId());
        }
    }

    private String agentExecutionRole(User submitter) {
        String role = submitter == null ? null : submitter.getIdentity();
        if ("ADMIN".equalsIgnoreCase(role) || "OPS".equalsIgnoreCase(role) || "OPERATOR".equalsIgnoreCase(role)) {
            return role;
        }
        return "OPERATOR";
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
