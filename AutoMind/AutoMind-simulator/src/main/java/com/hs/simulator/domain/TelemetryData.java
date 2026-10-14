package com.hs.simulator.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("telemetry_data")
public class TelemetryData {

    @TableField("time")
    private LocalDateTime time;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("link_id")
    private String linkId;

    @TableField("link_code")
    private String linkCode;

    @TableField("latency_ms")
    private BigDecimal latencyMs;

    @TableField("packet_loss_rate")
    private BigDecimal packetLossRate;

    @TableField("bandwidth_usage")
    private BigDecimal bandwidthUsage;

    @TableField("jitter_ms")
    private BigDecimal jitterMs;

    @TableField("is_anomaly")
    private Boolean isAnomaly;
}
