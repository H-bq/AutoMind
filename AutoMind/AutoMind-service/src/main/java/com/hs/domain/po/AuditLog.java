package com.hs.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("audit_log")
@Schema(name = "AuditLog", description = "")
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "租户id")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "用户id/操作员id")
    @TableField("actor_id")
    private Integer actorId;

    @Schema(description = "操作人角色/职位")
    @TableField("actor_role")
    private String actorRole;

    @Schema(description = "操作类型")
    @TableField("action")
    private String action;

    @TableField("created_at")
    private LocalDateTime createdAt;


}
