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
@TableName("mcp_call_log")
@Schema(name = "McpCallLog", description = "MCP 调用日志")
public class McpCallLog implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("agent_task_id")
    private Long agentTaskId;

    @TableField("intent_id")
    private Integer intentId;

    @TableField("trace_id")
    private String traceId;

    @TableField("tool_name")
    private String toolName;

    @TableField("tool_version")
    private String toolVersion;

    @TableField("request_params")
    private String requestParams;

    @TableField("response_code")
    private Integer responseCode;

    @TableField("response_body")
    private String responseBody;

    @TableField("error_type")
    private String errorType;

    @TableField("latency_ms")
    private Integer latencyMs;

    @TableField("attempt_no")
    private Integer attemptNo;

    @TableField("is_high_risk")
    private Boolean isHighRisk;

    @TableField("called_at")
    private LocalDateTime calledAt;
}
