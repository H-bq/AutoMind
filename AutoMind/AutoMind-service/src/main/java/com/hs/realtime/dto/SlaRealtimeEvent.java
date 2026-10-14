package com.hs.realtime.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Schema(description = "实时 SLA 推送事件")
public class SlaRealtimeEvent {

    @Schema(description = "租户标识", example = "tenant-001")
    private String tenantId;

    @Schema(description = "链路标识", example = "30000000-0000-0000-0000-000000000001")
    private String linkId;

    @Schema(description = "链路编码", example = "bj-office-link-01")
    private String linkCode;

    @Schema(description = "人员位置")
    private String position;

    @Schema(description = "指标类型", example = "LATENCY")
    private String metricType;

    @Schema(description = "观测值", example = "48.5000")
    private BigDecimal observedValue;

    @Schema(description = "目标值", example = "50.0000")
    private BigDecimal targetValue;

    @Schema(description = "是否违约", example = "false")
    private Boolean breachFlag;

    @Schema(description = "评估窗口", example = "5m")
    private String evaluationWindow;

    @Schema(description = "评估时间")
    private LocalDateTime evaluatedAt;

    @Schema(description = "事件来源", example = "SIMULATOR")
    private String source;
}
