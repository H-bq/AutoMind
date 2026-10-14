package com.hs.agent.exception;

import com.hs.common.exception.CommonException;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class LowConfidenceException extends CommonException {

    private final BigDecimal actualConfidence;
    private final BigDecimal threshold;
    private final String originalPayload;

    public LowConfidenceException(BigDecimal actualConfidence, BigDecimal threshold, String originalPayload) {
        super("大模型输出置信度不足", 422);
        this.actualConfidence = actualConfidence;
        this.threshold = threshold;
        this.originalPayload = originalPayload;
    }
}
