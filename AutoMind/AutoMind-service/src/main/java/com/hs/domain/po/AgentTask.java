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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("agent_task")
@Schema(name = "AgentTask", description = "Agent 任务记录")
public class AgentTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("intent_id")
    private Integer intentId;

    @TableField("parent_task_id")
    private Long parentTaskId;

    @TableField("trace_id")
    private String traceId;

    @TableField("agent_role")
    private String agentRole;

    @TableField("task_type")
    private String taskType;

    @TableField("pev_state")
    private String pevState;

    @TableField("confidence")
    private BigDecimal confidence;

    @TableField("input_payload")
    private String inputPayload;

    @TableField("output_payload")
    private String outputPayload;

    @TableField("error_code")
    private String errorCode;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("started_at")
    private LocalDateTime startedAt;

    @TableField("finished_at")
    private LocalDateTime finishedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
