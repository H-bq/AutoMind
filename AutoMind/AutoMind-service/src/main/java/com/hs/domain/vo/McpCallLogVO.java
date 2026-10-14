package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Schema(description = "MCP 调用日志视图")
public class McpCallLogVO {

    private Long id;
    private String tenantId;
    private Long agentTaskId;
    private Integer intentId;
    private String traceId;
    private String toolName;
    private String toolVersion;
    private String requestParams;
    private Integer responseCode;
    private String responseBody;
    private String errorType;
    private Integer latencyMs;
    private Integer attemptNo;
    private Boolean isHighRisk;
    private LocalDateTime calledAt;
}
