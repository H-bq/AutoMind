package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Schema(description = "审批记录视图")
public class ApprovalRecordVO {
    private Long id;
    private Integer intentId;
    private Long agentTaskId;
    private String traceId;
    private Integer approverId;
    private String tenantId;
    private String approvalType;
    private String riskLevel;
    private String aiRecommendation;
    private String submittedPayload;
    private String modifiedPayload;
    private String status;
    private String decisionReason;
    private LocalDateTime submittedAt; // 提交时间
    private LocalDateTime decidedAt; // 审批决策时间
    private LocalDateTime expireAt;  // 过期时间
}
