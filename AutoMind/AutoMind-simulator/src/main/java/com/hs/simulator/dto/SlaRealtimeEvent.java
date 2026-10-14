package com.hs.simulator.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class SlaRealtimeEvent {

    private String tenantId;
    private String linkId;
    private String linkCode;
    private String metricType;
    private BigDecimal observedValue;
    private BigDecimal targetValue;
    private Boolean breachFlag;
    private String evaluationWindow;
    private LocalDateTime evaluatedAt;
    private String source;
}
