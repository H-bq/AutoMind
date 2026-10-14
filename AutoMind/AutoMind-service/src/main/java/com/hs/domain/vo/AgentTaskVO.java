package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Schema(description = "Agent 任务视图")
public class AgentTaskVO {

    private Long id;
    private String tenantId;
    private Integer intentId;
    private Long parentTaskId;
    private String traceId;
    private String agentRole;
    private String taskType;
    private String pevState;
    private BigDecimal confidence;
    private String inputPayload;
    private String outputPayload;
    private String errorCode;
    private Integer retryCount;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private AgentTaskDecisionSummaryVO decisionSummary;
}
