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
@TableName("sla_record")
@Schema(name = "SlaRecord", description = "SLA 评估记录")
public class SlaRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "租户ID")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "关联意图ID")
    @TableField("intent_id")
    private Integer intentId;

    @Schema(description = "链路ID")
    @TableField("link_id")
    private String linkId;

    @Schema(description = "指标类型")
    @TableField("metric_type")
    private String metricType;

    @Schema(description = "目标值")
    @TableField("target_value")
    private BigDecimal targetValue;

    @Schema(description = "观测值")
    @TableField("observed_value")
    private BigDecimal observedValue;

    @Schema(description = "是否违约")
    @TableField("breach_flag")
    private Boolean breachFlag;

    @Schema(description = "评估窗口")
    @TableField("evaluation_window")
    private String evaluationWindow;

    @Schema(description = "评估时间")
    @TableField("evaluated_at")
    private LocalDateTime evaluatedAt;
}
