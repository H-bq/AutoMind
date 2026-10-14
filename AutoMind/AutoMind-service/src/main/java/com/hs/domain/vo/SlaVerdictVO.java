package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Schema(description = "SLA 评估结论")
public class SlaVerdictVO {

    @Schema(description = "链路ID", example = "link-001")
    private String linkId;

    @Schema(description = "指标类型", example = "LATENCY")
    private String metricType;

    @Schema(description = "观测值", example = "23.5000")
    private BigDecimal observedValue;

    @Schema(description = "目标值", example = "50.0000")
    private BigDecimal targetValue;

    @Schema(description = "是否违约", example = "false")
    private Boolean breachFlag;

    @Schema(description = "评估窗口", example = "5m")
    private String evaluationWindow;

    @Schema(description = "评估时间")
    private LocalDateTime evaluatedAt;
}
