package com.hs.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "审批记录查询参数")
public class ApprovalQueryDTO extends PageQueryDTO {

    @Schema(description = "审批类型", example = "HIGH_RISK_MCP")
    private String approvalType;

    @Schema(description = "风险等级", example = "HIGH")
    private String riskLevel;

    @Schema(description = "审批状态", example = "PENDING")
    private String status;

    @Schema(description = "TraceId")
    private String traceId;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
