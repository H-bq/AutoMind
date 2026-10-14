package com.hs.domain.vo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hs.agent.dto.ParsedIntent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Schema(description = "审批详情")
public class ApprovalDetailVO {
    private String tenantId; // 租户ID
    private String position; // 租户名
    private String inputText;  // 输入文本
    private ParsedIntent parsedIntent; // 意图解析
    @JsonIgnore
    private String parsedIntentJson; // 数据库中的意图解析 JSON
    private String priority; // 优先级
    private String status; // 状态
    private String approvalType; // 审批类型
    private String riskLevel; // 风险等级
    private String aiRecommendation; // AI推荐
    private String submittedPayload;    // 原始payload
    private String modifiedPayload; // 修改后的payload
    private String decisionReason; // 审批结果
    private LocalDateTime submittedAt; // 提交时间
    private LocalDateTime decidedAt; // 审批决策时间
}
