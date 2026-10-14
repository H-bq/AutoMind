package com.hs.healing.event;

import com.hs.agent.core.AgentContext;
import com.hs.agent.dto.SlaVerdict;
import org.springframework.context.ApplicationEvent;

public class SlaBreachEvent extends ApplicationEvent {

    private final AgentContext context;
    private final SlaVerdict verdict;

    public SlaBreachEvent(Object source, AgentContext context, SlaVerdict verdict) {
        super(source);
        this.context = context;
        this.verdict = verdict;
    }

    public AgentContext context() {
        return context;
    }

    public SlaVerdict verdict() {
        return verdict;
    }
}
