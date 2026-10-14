package com.hs.agent.orchestrator;

import com.hs.agent.dto.SlaVerdict;
import org.springframework.stereotype.Component;

@Component
public class PevStateMachine {

    public boolean shouldReplan(SlaVerdict verdict, int retryCount, int maxRetries) {
        return verdict != null
                && "REPLAN".equalsIgnoreCase(verdict.getRecommendation())
                && retryCount < maxRetries;
    }
}
