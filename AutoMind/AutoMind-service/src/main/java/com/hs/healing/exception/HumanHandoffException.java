package com.hs.healing.exception;

import com.hs.common.exception.CommonException;
import com.hs.healing.HealingTrigger;
import lombok.Getter;

@Getter
public class HumanHandoffException extends CommonException {

    private final Integer intentId;
    private final HealingTrigger lastTrigger;
    private final Integer attemptCount;
    private final String traceId;

    public HumanHandoffException(Integer intentId, HealingTrigger lastTrigger, Integer attemptCount, String traceId) {
        super("human handoff required", 503);
        this.intentId = intentId;
        this.lastTrigger = lastTrigger;
        this.attemptCount = attemptCount;
        this.traceId = traceId;
    }
}
