package com.hs.agent.dto;

import com.hs.agent.core.Confident;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Monitor Agent SLA 验证建议")
public class SlaVerdict implements Confident {

    private List<MetricResult> metricResults = new ArrayList<>();
    private Boolean overallBreach;
    private String worstMetric;
    private String recommendation;
    private BigDecimal confidence;

    @Data
    public static class MetricResult {
        private String linkId;
        private String metricType;
        private BigDecimal observedValue;
        private BigDecimal targetValue;
        private Boolean breachFlag;
    }
}
