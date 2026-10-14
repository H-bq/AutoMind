package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Schema(description = "当前用户提交意图的审批进度视图")
public class MyApprovalProgressVO {

    @Schema(description = "审批 ID")
    private Long approvalId;

    @Schema(description = "意图 ID")
    private Integer intentId;

    @Schema(description = "租户 ID")
    private String tenantId;

    @Schema(description = "提交用户 ID")
    private Integer userId;

    @Schema(description = "原始意图文本")
    private String inputText;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "意图状态")
    private String intentStatus;

    @Schema(description = "TraceId")
    private String traceId;

    @Schema(description = "意图提交时间")
    private LocalDateTime intentSubmittedAt;

    @Schema(description = "审批类型")
    private String approvalType;

    @Schema(description = "风险等级")
    private String riskLevel;

    @Schema(description = "审批状态")
    private String approvalStatus;

    @Schema(description = "审批原因")
    private String decisionReason;

    @Schema(description = "进入审批时间")
    private LocalDateTime approvalSubmittedAt;

    @Schema(description = "审批决策时间")
    private LocalDateTime decidedAt;

    @Schema(description = "审批过期时间")
    private LocalDateTime expireAt;
}
