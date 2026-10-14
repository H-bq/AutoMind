package com.hs.agent.orchestrator;

import com.hs.approval.ApprovalStatus;
import com.hs.approval.event.ApprovalDecidedEvent;
import com.hs.domain.po.ApprovalRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalResumeListener {

    private final IntentOrchestrationService orchestrationService;

    @EventListener
    public void onApprovalDecided(ApprovalDecidedEvent event) {
        ApprovalRecord approval = event.approval();
        if (approval == null || approval.getIntentId() == null) {
            return;
        }
        if (ApprovalStatus.APPROVED.name().equals(approval.getStatus())) {
            log.info("Approval approved, resume intentId={}, approvalId={}", approval.getIntentId(), approval.getId());
            orchestrationService.resume(approval.getIntentId());
            return;
        }
        if (ApprovalStatus.REJECTED.name().equals(approval.getStatus())
                || ApprovalStatus.AUTO_TIMEOUT.name().equals(approval.getStatus())) {
            log.info("Approval rejected, mark intent rejected intentId={}, approvalId={}", approval.getIntentId(), approval.getId());
            orchestrationService.rejectForApproval(approval.getIntentId(), approval.getDecisionReason());
        }
    }
}
