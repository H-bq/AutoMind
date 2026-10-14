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
 * 网络链路拓扑与基础容量信息表
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("network_link")
@Schema(name = "NetworkLink", description = "网络链路拓扑与基础容量信息表")
public class NetworkLink implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "网络链路唯一标识")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @Schema(description = "租户标识，用于多租户隔离")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "链路编码")
    @TableField("link_code")
    private String linkCode;

    @Schema(description = "链路名称")
    @TableField("link_name")
    private String linkName;

    @Schema(description = "源节点标识")
    @TableField("source_node_id")
    private String sourceNodeId;

    @Schema(description = "目标节点标识")
    @TableField("target_node_id")
    private String targetNodeId;

    @Schema(description = "链路类型，如主链路或备用链路")
    @TableField("link_type")
    private String linkType;

    @Schema(description = "链路总带宽，单位 Mbps")
    @TableField("total_bandwidth_mbps")
    private BigDecimal totalBandwidthMbps;

    @Schema(description = "已预留带宽，单位 Mbps")
    @TableField("reserved_bandwidth_mbps")
    private BigDecimal reservedBandwidthMbps;

    @Schema(description = "链路目标最大时延，单位毫秒")
    @TableField("max_latency_ms")
    private BigDecimal maxLatencyMs;

    @Schema(description = "链路运行状态")
    @TableField("status")
    private String status;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;


}
