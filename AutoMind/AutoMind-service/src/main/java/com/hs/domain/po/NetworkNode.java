package com.hs.domain.po;

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
 * 网络节点信息表
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("network_node")
@Schema(name = "NetworkNode", description = "网络节点信息表")
public class NetworkNode implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "网络节点唯一标识")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @Schema(description = "租户标识，用于多租户隔离")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "所属网络域标识")
    @TableField("domain_id")
    private String domainId;

    @Schema(description = "网络节点编码")
    @TableField("node_code")
    private String nodeCode;

    @Schema(description = "网络节点名称")
    @TableField("node_name")
    private String nodeName;

    @Schema(description = "网络节点类型，如 SWITCH")
    @TableField("node_type")
    private String nodeType;

    @Schema(description = "节点所在位置")
    @TableField("location")
    private String location;

    @Schema(description = "节点管理 IP")
    @TableField("management_ip")
    private String managementIp;

    @Schema(description = "节点运行状态")
    @TableField("status")
    private String status;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;


}
