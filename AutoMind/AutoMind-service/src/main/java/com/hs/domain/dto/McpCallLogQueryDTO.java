package com.hs.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "MCP 调用日志分页查询参数")
public class McpCallLogQueryDTO extends PageQueryDTO {

    @Schema(description = "TraceId", example = "7d9f...")
    private String traceId;

    @Schema(description = "Agent 任务 ID", example = "1001")
    private Long agentTaskId;

    @Schema(description = "MCP 工具名", example = "set_qos")
    private String toolName;

    @Schema(description = "错误类型", example = "TIMEOUT")
    private String errorType;

    @Schema(description = "是否高风险工具", example = "true")
    private Boolean isHighRisk;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
