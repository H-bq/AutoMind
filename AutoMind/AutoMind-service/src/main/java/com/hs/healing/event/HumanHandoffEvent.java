package com.hs.healing.event;

import com.hs.agent.core.AgentContext;
import com.hs.healing.HealingTrigger;
import org.springframework.context.ApplicationEvent;

public class HumanHandoffEvent extends ApplicationEvent {

    private final AgentContext context;
    private final HealingTrigger trigger;
    private final String rootCause;
    private final Integer attemptCount;

    public HumanHandoffEvent(Object source, AgentContext context, HealingTrigger trigger, String rootCause, Integer attemptCount) {
        super(source);
        this.context = context;
        this.trigger = trigger;
        this.rootCause = rootCause;
        this.attemptCount = attemptCount;
    }

    public AgentContext context() {
        return context;
    }

    public HealingTrigger trigger() {
        return trigger;
    }

    public String rootCause() {
        return rootCause;
    }

    public Integer attemptCount() {
        return attemptCount;
    }
}
