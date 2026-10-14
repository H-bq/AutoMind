package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Schema(description = "自愈记录视图")
public class HealingRecordVO {
    private Long id;
    private String tenantId;
    private Integer intentId;
    private Long triggerAgentTaskId;
    private String traceId;
    private String triggerSource;
    private String rootCause;
    private Integer attemptNo;
    private Integer maxAttempts;
    private String strategy;
    private String result;
    private LocalDateTime recoveredAt;
    private LocalDateTime createdAt;
}
