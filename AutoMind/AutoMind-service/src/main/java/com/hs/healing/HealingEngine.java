package com.hs.healing;

import com.hs.a2a.A2aMessage;
import com.hs.a2a.event.A2aDegradedEvent;
import com.hs.agent.core.AgentContext;
import com.hs.agent.orchestrator.IntentOrchestrationService;
import com.hs.common.trace.TraceIdHolder;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.AgentTask;
import com.hs.domain.po.HealingRecord;
import com.hs.healing.event.HumanHandoffEvent;
import com.hs.healing.event.SlaBreachEvent;
import com.hs.mcp.event.McpTimeoutEvent;
import com.hs.service.IAgentTaskService;
import com.hs.service.IHealingRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealingEngine {

    private final IHealingRecordService healingRecordService;
    private final IAgentTaskService agentTaskService;
    private final IntentOrchestrationService orchestrationService;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<Integer, Boolean> activeIntents = new ConcurrentHashMap<>();

    @Value("${automind.healing.max-attempts:3}")
    private Integer maxAttempts;

    @EventListener
    public void onSlaBreach(SlaBreachEvent event) {
        heal(event.context(), HealingTrigger.MONITOR, "SLA breach: " + event.verdict().getWorstMetric());
    }

    @EventListener
    public void onMcpTimeout(McpTimeoutEvent event) {
        heal(event.context().agent(), HealingTrigger.MCP_ERROR, "MCP timeout: " + event.context().toolName());
    }

    @EventListener
    public void onA2aDegraded(A2aDegradedEvent event) {
        AgentContext ctx = contextFromA2a(event.message());
        if (ctx != null) {
            heal(ctx, HealingTrigger.A2A_FAIL, "A2A degraded");
        }
    }

    @Async("agentExecutor")
    @Transactional
    public void heal(AgentContext ctx, HealingTrigger trigger, String rootCause) {
        if (ctx == null || ctx.intentId() == null) {
            return;
        }
        if (activeIntents.putIfAbsent(ctx.intentId(), Boolean.TRUE) != null) {
            log.info("Healing already active intentId={}, trigger={}", ctx.intentId(), trigger);
            return;
        }
        try {
            long attempts = healingRecordService.lambdaQuery()
                    .eq(HealingRecord::getIntentId, ctx.intentId())
                    .count();
            int max = maxAttempts == null ? 3 : maxAttempts;
            if (attempts >= max) {
                HealingRecord handoff = baseRecord(ctx, trigger, rootCause)
                        .setAttemptNo((int) attempts + 1)
                        .setMaxAttempts(max)
                        .setResult("HANDOFF_HUMAN")
                        .setRecoveredAt(LocalDateTime.now());
                healingRecordService.save(handoff);
                eventPublisher.publishEvent(new HumanHandoffEvent(this, ctx, trigger, rootCause, (int) attempts + 1));
                return;
            }
            HealingRecord record = baseRecord(ctx, trigger, rootCause)
                    .setAttemptNo((int) attempts + 1)
                    .setMaxAttempts(max)
                    .setStrategy(trigger == HealingTrigger.A2A_FAIL ? "LOCAL_FALLBACK_REPLAN" : "REPLAN");
            healingRecordService.save(record);

            TraceIdHolder.set(ctx.traceId());
            UserContext.setUserInfo(ctx.userId(), ctx.tenantId(), ctx.role(), ctx.intentId());
            orchestrationService.replan(ctx.intentId());
            healingRecordService.lambdaUpdate()
                    .eq(HealingRecord::getId, record.getId())
                    .set(HealingRecord::getResult, "SUCCESS")
                    .set(HealingRecord::getRecoveredAt, LocalDateTime.now())
                    .update();
        } catch (Exception e) {
            log.error("Healing failed intentId={}, trigger={}", ctx.intentId(), trigger, e);
        } finally {
            activeIntents.remove(ctx.intentId());
        }
    }

    private HealingRecord baseRecord(AgentContext ctx, HealingTrigger trigger, String rootCause) {
        return new HealingRecord()
                .setTenantId(ctx.tenantId())
                .setIntentId(ctx.intentId())
                .setTriggerAgentTaskId(ctx.agentTaskId())
                .setTraceId(ctx.traceId())
                .setTriggerSource(trigger.name())
                .setRootCause(rootCause)
                .setCreatedAt(LocalDateTime.now());
    }

    private AgentContext contextFromA2a(A2aMessage message) {
        if (message == null || message.taskId() == null) {
            return null;
        }
        AgentTask task = agentTaskService.getById(message.taskId());
        if (task == null) {
            return null;
        }
        return new AgentContext(task.getId(), task.getParentTaskId(), task.getIntentId(),
                task.getTenantId(), UserContext.role(), UserContext.getUser(),
                message.traceId(), task.getAgentRole());
    }
}
