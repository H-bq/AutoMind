package com.hs.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SLA 评估记录分页查询参数")
public class SlaRecordQueryDTO extends PageQueryDTO {

    @Schema(description = "链路ID", example = "link-001")
    private String linkId;

    @Schema(description = "指标类型：LATENCY/JITTER/LOSS/BANDWIDTH", example = "LATENCY")
    private String metricType;

    @Schema(description = "是否违约", example = "false")
    private Boolean breachFlag;
}
