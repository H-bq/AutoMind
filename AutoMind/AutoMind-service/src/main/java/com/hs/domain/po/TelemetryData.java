package com.hs.domain.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 网络链路遥测数据表
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("telemetry_data")
@Schema(name = "TelemetryData", description = "网络链路遥测数据表")
public class TelemetryData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "遥测采集时间")
    @TableField("time")
    private LocalDateTime time;

    @Schema(description = "租户标识，用于多租户隔离")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "关联链路标识")
    @TableField("link_id")
    private String linkId;

    @Schema(description = "链路编码冗余字段，便于查询展示")
    @TableField("link_code")
    private String linkCode;

    @Schema(description = "链路时延，单位毫秒")
    @TableField("latency_ms")
    private BigDecimal latencyMs;

    @Schema(description = "链路丢包率")
    @TableField("packet_loss_rate")
    private BigDecimal packetLossRate;

    @Schema(description = "链路带宽使用率，单位百分比")
    @TableField("bandwidth_usage")
    private BigDecimal bandwidthUsage;

    @Schema(description = "链路抖动，单位毫秒")
    @TableField("jitter_ms")
    private BigDecimal jitterMs;

    @Schema(description = "是否为异常遥测点")
    @TableField("is_anomaly")
    private Boolean isAnomaly;


}
