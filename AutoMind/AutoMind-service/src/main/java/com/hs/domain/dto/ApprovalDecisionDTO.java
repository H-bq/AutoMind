package com.hs.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "审批决策请求")
public class ApprovalDecisionDTO {

    @Schema(description = "审批原因", example = "风险已确认，允许执行")
    private String reason;
}
