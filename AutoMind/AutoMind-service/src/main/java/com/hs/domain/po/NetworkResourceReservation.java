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
 * 网络资源预留记录表
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("network_resource_reservation")
@Schema(name = "NetworkResourceReservation", description = "网络资源预留记录表")
public class NetworkResourceReservation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "资源预留唯一标识")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @Schema(description = "租户标识，用于多租户隔离")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "关联意图请求标识")
    @TableField("intent_request_id")
    private Integer intentRequestId;

    @Schema(description = "关联链路标识")
    @TableField("link_id")
    private String linkId;

    @Schema(description = "预留带宽，单位 Mbps")
    @TableField("reserved_bandwidth_mbps")
    private BigDecimal reservedBandwidthMbps;

    @Schema(description = "目标时延，单位毫秒")
    @TableField("target_latency_ms")
    private BigDecimal targetLatencyMs;

    @Schema(description = "目标丢包率")
    @TableField("target_packet_loss_rate")
    private BigDecimal targetPacketLossRate;

    @Schema(description = "资源预留优先级，数值越小优先级越高")
    @TableField("priority")
    private Integer priority;

    @Schema(description = "资源预留状态")
    @TableField("reservation_status")
    private String reservationStatus;

    @Schema(description = "预留生效开始时间")
    @TableField("effective_start")
    private LocalDateTime effectiveStart;

    @Schema(description = "预留生效结束时间")
    @TableField("effective_end")
    private LocalDateTime effectiveEnd;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;


}
