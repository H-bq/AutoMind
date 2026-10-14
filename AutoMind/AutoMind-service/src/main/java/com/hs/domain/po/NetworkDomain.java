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
 * 网络域与控制器域信息表
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("network_domain")
@Schema(name = "NetworkDomain", description = "网络域与控制器域信息表")
public class NetworkDomain implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "网络域唯一标识")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @Schema(description = "租户标识，用于多租户隔离")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "网络域编码")
    @TableField("domain_code")
    private String domainCode;

    @Schema(description = "网络域名称")
    @TableField("domain_name")
    private String domainName;

    @Schema(description = "控制器类型，如 RYU 或 ONOS")
    @TableField("controller_type")
    private String controllerType;

    @Schema(description = "模拟 SDN 控制器访问地址")
    @TableField("controller_endpoint")
    private String controllerEndpoint;

    @Schema(description = "网络域状态")
    @TableField("status")
    private String status;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;


}
