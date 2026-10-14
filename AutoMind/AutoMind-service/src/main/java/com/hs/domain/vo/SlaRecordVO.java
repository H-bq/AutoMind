package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "SLA 评估历史记录")
public class SlaRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "意图ID")
    private Integer intentId;

    @Schema(description = "链路ID")
    private String linkId;

    @Schema(description = "人员位置")
    private String position;

    @Schema(description = "指标类型")
    private String metricType;

    @Schema(description = "目标值")
    private BigDecimal targetValue;

    @Schema(description = "观测值")
    private BigDecimal observedValue;

    @Schema(description = "是否违约")
    private Boolean breachFlag;

    @Schema(description = "评估窗口")
    private String evaluationWindow;

    @Schema(description = "评估时间")
    private LocalDateTime evaluatedAt;
}
