package com.hs.service.impl;

import com.hs.common.exception.CommonException;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.*;
import com.hs.domain.vo.IntentFullTraceVO;
import com.hs.domain.vo.ReadModelVO;
import com.hs.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IntentFullTraceServiceImpl implements IntentFullTraceService {

    private final IIntentRequestService intentRequestService;
    private final IAgentTaskService agentTaskService;
    private final IMcpCallLogService mcpCallLogService;
    private final IA2aMessageService a2aMessageService;
    private final IApprovalRecordService approvalRecordService;
    private final ISlaRecordService slaRecordService;
    private final IHealingRecordService healingRecordService;
    private final IAuditLogService auditLogService;

    @Override
    public IntentFullTraceVO byIntentId(Integer intentId) {
        IntentRequest intent = intentRequestService.getById(intentId);
        return build(requireVisibleIntent(intent));
    }

    @Override
    public IntentFullTraceVO byTraceId(String traceId) {
        IntentRequest intent = intentRequestService.lambdaQuery()
                .eq(IntentRequest::getTraceId, traceId)
                .last("LIMIT 1")
                .one();
        if (intent == null) {
            AgentTask task = agentTaskService.lambdaQuery()
                    .eq(AgentTask::getTraceId, traceId)
                    .orderByAsc(AgentTask::getStartedAt)
                    .last("LIMIT 1")
                    .one();
            if (task != null) {
                intent = intentRequestService.getById(task.getIntentId());
            }
        }
        return build(requireVisibleIntent(intent));
    }

    private IntentRequest requireVisibleIntent(IntentRequest intent) {
        String tenantId = UserContext.requireTenantId();
        if (intent == null) {
            throw new CommonException("意图请求不存在", 404);
        }
        if (!UserContext.isAdminTenant(tenantId) && !tenantId.equals(intent.getTenantId())) {
            auditLogService.save(new AuditLog()
                    .setTenantId(tenantId)
                    .setActorId(UserContext.getUser())
                    .setActorRole(UserContext.role())
                    .setAction("TENANT_VIOLATION")
                    .setCreatedAt(LocalDateTime.now()));
            throw new CommonException("意图请求不存在", 404);
        }
        return intent;
    }

    private IntentFullTraceVO build(IntentRequest intent) {
        Integer intentId = intent.getId();
        String traceId = intent.getTraceId();
        List<AgentTask> tasks = agentTaskService.lambdaQuery()
                .eq(AgentTask::getIntentId, intentId)
                .orderByAsc(AgentTask::getStartedAt)
                .list();
        if ((traceId == null || traceId.isBlank()) && !tasks.isEmpty()) {
            traceId = tasks.get(0).getTraceId();
        }
        List<McpCallLog> mcpLogs = mcpCallLogService.lambdaQuery()
                .eq(McpCallLog::getIntentId, intentId)
                .orderByAsc(McpCallLog::getCalledAt)
                .list();
        List<A2aMessagePO> a2aMessages = traceId == null ? List.of() : a2aMessageService.lambdaQuery()
                .eq(A2aMessagePO::getTraceId, traceId)
                .orderByAsc(A2aMessagePO::getSentAt)
                .list();
        List<ApprovalRecord> approvals = approvalRecordService.lambdaQuery()
                .eq(ApprovalRecord::getIntentId, intentId)
                .orderByAsc(ApprovalRecord::getSubmittedAt)
                .list();
        List<SlaRecord> slaRecords = slaRecordService.lambdaQuery()
                .eq(SlaRecord::getIntentId, intentId)
                .orderByAsc(SlaRecord::getEvaluatedAt)
                .list();
        List<HealingRecord> healingRecords = healingRecordService.lambdaQuery()
                .eq(HealingRecord::getIntentId, intentId)
                .orderByAsc(HealingRecord::getCreatedAt)
                .list();

        IntentFullTraceVO vo = new IntentFullTraceVO()
                .setIntent(ReadModelVO.toIntentRequestVO(intent));
        tasks.forEach(task -> add(vo, task.getStartedAt(), "AGENT_TASK",
                task.getAgentRole() + "/" + task.getPevState(), task.getId(), task));
        mcpLogs.forEach(log -> add(vo, log.getCalledAt(), "MCP_CALL",
                log.getToolName() + "/" + nullToOk(log.getErrorType()), log.getId(), log));
        a2aMessages.forEach(message -> add(vo, message.getSentAt(), "A2A",
                message.getMessageType() + "/" + nullToOk(message.getStatus()), message.getId(), message));
        approvals.forEach(approval -> add(vo, approval.getSubmittedAt(), "APPROVAL",
                approval.getApprovalType() + "/" + approval.getStatus(), approval.getId(), approval));
        slaRecords.forEach(sla -> add(vo, sla.getEvaluatedAt(), "SLA",
                sla.getMetricType() + "/" + (Boolean.TRUE.equals(sla.getBreachFlag()) ? "BREACH" : "OK"), sla.getId(), sla));
        healingRecords.forEach(healing -> add(vo, healing.getCreatedAt(), "HEALING",
                healing.getTriggerSource() + "/" + nullToOk(healing.getResult()), healing.getId(), healing));
        vo.getTimeline().sort(Comparator.comparing(IntentFullTraceVO.TimelineItemVO::getTime,
                Comparator.nullsLast(Comparator.naturalOrder())));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("pevState", intent.getStatus());
        summary.put("healingAttempts", healingRecords.size());
        summary.put("approvalsPending", approvals.stream().filter(a -> "PENDING".equals(a.getStatus())).count());
        summary.put("slaBreaches", slaRecords.stream().filter(s -> Boolean.TRUE.equals(s.getBreachFlag())).count());
        vo.setSummary(summary);
        return vo;
    }

    private void add(IntentFullTraceVO vo, LocalDateTime time, String type, String summary, Object payloadId, Object payload) {
        vo.getTimeline().add(new IntentFullTraceVO.TimelineItemVO()
                .setTime(time)
                .setType(type)
                .setSummary(summary)
                .setPayloadId(payloadId)
                .setPayload(payload));
    }

    private String nullToOk(String value) {
        return value == null || value.isBlank() ? "OK" : value;
    }
}
