package com.hs.approval;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.agent.core.AgentContext;
import com.hs.approval.event.ApprovalDecidedEvent;
import com.hs.approval.event.ApprovalExpiredEvent;
import com.hs.approval.event.ApprovalRequestedEvent;
import com.hs.common.exception.CommonException;
import com.hs.domain.po.ApprovalRecord;
import com.hs.service.IApprovalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final IApprovalRecordService approvalRecordService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${automind.approval.expire-minutes:60}")
    private Integer expireMinutes;

    @Transactional
    public Long submit(AgentContext ctx, ApprovalType type, RiskLevel risk, Object aiRecommendation, Object submittedPayload) {
        Optional<ApprovalRecord> existing = findPendingFor(ctx.agentTaskId(), type)
                .or(() -> findApprovedForIntent(ctx.intentId(), type));
        if (existing.isPresent()) {
            return existing.get().getId();
        }
        ApprovalRecord record = new ApprovalRecord()
                .setTenantId(ctx.tenantId())
                .setIntentId(ctx.intentId())
                .setAgentTaskId(ctx.agentTaskId())
                .setTraceId(ctx.traceId())
                .setApprovalType(type.name())
                .setRiskLevel(risk == null ? RiskLevel.MEDIUM.name() : risk.name())
                .setAiRecommendation(toJson(aiRecommendation))
                .setSubmittedPayload(toJson(submittedPayload))
                .setStatus(ApprovalStatus.PENDING.name())
                .setSubmittedAt(LocalDateTime.now())
                .setExpireAt(LocalDateTime.now().plusMinutes(expireMinutes == null ? 60 : expireMinutes));
        approvalRecordService.save(record);
        eventPublisher.publishEvent(new ApprovalRequestedEvent(this, record));
        return record.getId();
    }

    public Optional<ApprovalRecord> findPendingFor(Long agentTaskId, ApprovalType type) {
        if (agentTaskId == null) {
            return Optional.empty();
        }
        return approvalRecordService.lambdaQuery()
                .eq(ApprovalRecord::getAgentTaskId, agentTaskId)
                .eq(ApprovalRecord::getApprovalType, type.name())
                .eq(ApprovalRecord::getStatus, ApprovalStatus.PENDING.name())
                .orderByDesc(ApprovalRecord::getSubmittedAt)
                .last("LIMIT 1")
                .oneOpt();
    }

    public Optional<ApprovalRecord> findLatestForTask(Long agentTaskId, ApprovalType type) {
        if (agentTaskId == null) {
            return Optional.empty();
        }
        return approvalRecordService.lambdaQuery()
                .eq(ApprovalRecord::getAgentTaskId, agentTaskId)
                .eq(ApprovalRecord::getApprovalType, type.name())
                .orderByDesc(ApprovalRecord::getSubmittedAt)
                .last("LIMIT 1")
                .oneOpt();
    }

    public Optional<ApprovalRecord> findApprovedForIntent(Integer intentId, ApprovalType type) {
        if (intentId == null) {
            return Optional.empty();
        }
        return approvalRecordService.lambdaQuery()
                .eq(ApprovalRecord::getIntentId, intentId)
                .eq(ApprovalRecord::getApprovalType, type.name())
                .eq(ApprovalRecord::getStatus, ApprovalStatus.APPROVED.name())
                .gt(ApprovalRecord::getExpireAt, LocalDateTime.now())
                .orderByDesc(ApprovalRecord::getDecidedAt)
                .last("LIMIT 1")
                .oneOpt();
    }

    @Transactional
    public void decide(Long approvalId, ApprovalStatus status, Integer approverId, String reason) {
        ApprovalRecord existing = approvalRecordService.getById(approvalId);
        if (existing == null) {
            throw new CommonException("审批记录不存在", 404);
        }
        boolean updated = approvalRecordService.update(new LambdaUpdateWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getId, approvalId)
                .eq(ApprovalRecord::getStatus, ApprovalStatus.PENDING.name())
                .set(ApprovalRecord::getStatus, status.name())
                .set(ApprovalRecord::getApproverId, approverId)
                .set(ApprovalRecord::getDecisionReason, reason)
                .set(ApprovalRecord::getDecidedAt, LocalDateTime.now()));
        if (!updated) {
            throw new CommonException("审批记录已被处理", 409);
        }
        ApprovalRecord decided = approvalRecordService.getById(approvalId);
        eventPublisher.publishEvent(new ApprovalDecidedEvent(this, decided));
    }

    @Transactional
    public void rejectWithModification(Long approvalId, Integer approverId, String reason, Object modifiedPayload) {
        ApprovalRecord existing = approvalRecordService.getById(approvalId);
        if (existing == null) {
            throw new CommonException("审批记录不存在", 404);
        }
        boolean updated = approvalRecordService.update(new LambdaUpdateWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getId, approvalId)
                .eq(ApprovalRecord::getStatus, ApprovalStatus.PENDING.name())
                .set(ApprovalRecord::getStatus, ApprovalStatus.REJECTED.name())
                .set(ApprovalRecord::getApproverId, approverId)
                .set(ApprovalRecord::getDecisionReason, reason)
                .set(ApprovalRecord::getModifiedPayload, toJson(modifiedPayload))
                .set(ApprovalRecord::getDecidedAt, LocalDateTime.now()));
        if (!updated) {
            throw new CommonException("审批记录已被处理", 409);
        }
        eventPublisher.publishEvent(new ApprovalDecidedEvent(this, approvalRecordService.getById(approvalId)));
    }

    @Transactional
    public int markExpired() {
        List<ApprovalRecord> expired = approvalRecordService.lambdaQuery()
                .eq(ApprovalRecord::getStatus, ApprovalStatus.PENDING.name())
                .lt(ApprovalRecord::getExpireAt, LocalDateTime.now())
                .list();
        int count = 0;
        for (ApprovalRecord record : expired) {
            boolean updated = approvalRecordService.update(new LambdaUpdateWrapper<ApprovalRecord>()
                    .eq(ApprovalRecord::getId, record.getId())
                    .eq(ApprovalRecord::getStatus, ApprovalStatus.PENDING.name())
                    .set(ApprovalRecord::getStatus, ApprovalStatus.AUTO_TIMEOUT.name())
                    .set(ApprovalRecord::getDecisionReason, "approval timeout")
                    .set(ApprovalRecord::getDecidedAt, LocalDateTime.now()));
            if (updated) {
                count++;
                ApprovalRecord latest = approvalRecordService.getById(record.getId());
                eventPublisher.publishEvent(new ApprovalExpiredEvent(this, latest));
                eventPublisher.publishEvent(new ApprovalDecidedEvent(this, latest));
            }
        }
        return count;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{\"serializationError\":\"" + e.getMessage() + "\"}";
        }
    }
}
