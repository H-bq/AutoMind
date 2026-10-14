package com.hs.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "SLA 单次评估请求")
public class SlaEvaluateDTO {

    @Schema(description = "链路ID", example = "link-001")
    private String linkId;

    @Schema(description = "指标类型：LATENCY/JITTER/LOSS/BANDWIDTH", example = "LATENCY")
    private String metricType;

    @Schema(description = "评估窗口，如 5m/1h；缺省使用系统默认窗口", example = "5m")
    private String window;
}
