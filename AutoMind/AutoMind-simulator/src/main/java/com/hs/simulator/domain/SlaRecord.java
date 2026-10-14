package com.hs.simulator.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("sla_record")
public class SlaRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("intent_id")
    private Integer intentId;

    @TableField("link_id")
    private String linkId;

    @TableField("metric_type")
    private String metricType;

    @TableField("target_value")
    private BigDecimal targetValue;

    @TableField("observed_value")
    private BigDecimal observedValue;

    @TableField("breach_flag")
    private Boolean breachFlag;

    @TableField("evaluation_window")
    private String evaluationWindow;

    @TableField("evaluated_at")
    private LocalDateTime evaluatedAt;
}
