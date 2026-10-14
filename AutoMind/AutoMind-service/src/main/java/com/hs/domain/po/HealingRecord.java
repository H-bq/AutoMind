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
@TableName("healing_record")
@Schema(name = "HealingRecord", description = "自愈记录")
public class HealingRecord implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("intent_id")
    private Integer intentId;

    @TableField("trigger_agent_task_id")
    private Long triggerAgentTaskId;

    @TableField("trace_id")
    private String traceId;

    @TableField("trigger_source")
    private String triggerSource;

    @TableField("root_cause")
    private String rootCause;

    @TableField("attempt_no")
    private Integer attemptNo;

    @TableField("max_attempts")
    private Integer maxAttempts;

    @TableField("strategy")
    private String strategy;

    @TableField("result")
    private String result;

    @TableField("recovered_at")
    private LocalDateTime recoveredAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
