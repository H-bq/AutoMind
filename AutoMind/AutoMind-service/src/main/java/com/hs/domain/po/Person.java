package com.hs.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("person")
@Schema(name = "Person", description = "")
public class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("user_id")
    private Integer userId;

    @TableField("position")
    private String position;

    @Schema(description = "租户id")
    @TableField("tenant_id")
    private String tenantId;

    @TableField("name")
    private String name;

    @TableField("sex")
    private String sex;

    @TableField("introduce")
    private String introduce;

    @TableField("label")
    private String label;


}
