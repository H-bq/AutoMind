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
@TableName("a2a_message")
@Schema(name = "A2aMessagePO", description = "A2A 跨域消息审计")
public class A2aMessagePO implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("source_domain_id")
    private String sourceDomainId;

    @TableField("target_domain_id")
    private String targetDomainId;

    @TableField("source_agent")
    private String sourceAgent;

    @TableField("target_agent")
    private String targetAgent;

    @TableField("task_id")
    private Long taskId;

    @TableField("trace_id")
    private String traceId;

    @TableField("message_type")
    private String messageType;

    @TableField("payload")
    private String payload;

    @TableField("status")
    private String status;

    @TableField("latency_ms")
    private Integer latencyMs;

    @TableField("sent_at")
    private LocalDateTime sentAt;

    @TableField("received_at")
    private LocalDateTime receivedAt;
}
