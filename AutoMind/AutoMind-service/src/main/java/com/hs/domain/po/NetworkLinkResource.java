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
 * 链路当前资源与 QoS 状态表
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("network_link_resource")
@Schema(name = "NetworkLinkResource", description = "链路当前资源与 QoS 状态表")
public class NetworkLinkResource implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "链路资源状态唯一标识")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @Schema(description = "租户标识，用于多租户隔离")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "关联链路标识")
    @TableField("link_id")
    private String linkId;

    @Schema(description = "QoS 策略档位")
    @TableField("qos_profile")
    private String qosProfile;

    @Schema(description = "优先级队列")
    @TableField("priority_queue")
    private String priorityQueue;

    @Schema(description = "当前可用带宽，单位 Mbps")
    @TableField("available_bandwidth_mbps")
    private BigDecimal availableBandwidthMbps;

    @Schema(description = "当前时延，单位毫秒")
    @TableField("current_latency_ms")
    private BigDecimal currentLatencyMs;

    @Schema(description = "当前丢包率")
    @TableField("current_packet_loss_rate")
    private BigDecimal currentPacketLossRate;

    @Schema(description = "当前抖动，单位毫秒")
    @TableField("current_jitter_ms")
    private BigDecimal currentJitterMs;

    @Schema(description = "是否处于异常状态")
    @TableField("is_anomaly")
    private Boolean isAnomaly;

    @Schema(description = "资源状态采样时间")
    @TableField("sampled_at")
    private LocalDateTime sampledAt;


}
