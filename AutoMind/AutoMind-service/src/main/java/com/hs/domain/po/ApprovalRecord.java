package com.hs.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("approval_record")
@Schema(name = "ApprovalRecord", description = "审批记录")
public class ApprovalRecord implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("intent_id")
    private Integer intentId;

    @TableField("agent_task_id")
    private Long agentTaskId;

    @TableField("trace_id")
    private String traceId;

    @TableField("approver_id")
    private Integer approverId;

    @TableField("approval_type")
    private String approvalType;

    @TableField("risk_level")
    private String riskLevel;

    @TableField("ai_recommendation")
    private String aiRecommendation;

    @TableField("submitted_payload")
    private String submittedPayload;

    @TableField("modified_payload")
    private String modifiedPayload;

    @TableField("status")
    private String status;

    @TableField("decision_reason")
    private String decisionReason;

    @TableField("submitted_at")
    private LocalDateTime submittedAt;

    @TableField("decided_at")
    private LocalDateTime decidedAt;

    @TableField("expire_at")
    private LocalDateTime expireAt;
}
