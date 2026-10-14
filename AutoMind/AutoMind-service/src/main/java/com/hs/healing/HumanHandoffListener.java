package com.hs.healing;

import com.hs.domain.po.AgentTask;
import com.hs.domain.po.AuditLog;
import com.hs.domain.po.IntentRequest;
import com.hs.healing.event.HumanHandoffEvent;
import com.hs.service.IAgentTaskService;
import com.hs.service.IAuditLogService;
import com.hs.service.IIntentRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class HumanHandoffListener {

    private final IAuditLogService auditLogService;
    private final IIntentRequestService intentRequestService;
    private final IAgentTaskService agentTaskService;

    @EventListener
    public void onHumanHandoff(HumanHandoffEvent event) {
        if (event.context() == null) {
            return;
        }
        auditLogService.save(new AuditLog()
                .setTenantId(event.context().tenantId())
                .setActorId(event.context().userId())
                .setActorRole("SYSTEM")
                .setAction("HUMAN_HANDOFF")
                .setCreatedAt(LocalDateTime.now()));
        intentRequestService.lambdaUpdate()
                .eq(IntentRequest::getId, event.context().intentId())
                .set(IntentRequest::getStatus, "HANDOFF_HUMAN")
                .update();
        agentTaskService.lambdaUpdate()
                .eq(AgentTask::getIntentId, event.context().intentId())
                .set(AgentTask::getPevState, "FAILED")
                .set(AgentTask::getErrorCode, "HUMAN_HANDOFF")
                .update();
        log.error("Human handoff required intentId={}, trigger={}, attempts={}, rootCause={}",
                event.context().intentId(), event.trigger(), event.attemptCount(), event.rootCause());
    }
}
