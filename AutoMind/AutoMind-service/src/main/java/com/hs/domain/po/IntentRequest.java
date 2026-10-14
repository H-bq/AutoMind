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
 * 
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("intent_request")
@Schema(name = "IntentRequest", description = "")
public class IntentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "租户id")
    @TableField("tenant_id")
    private String tenantId;

    @TableField("user_id")
    private Integer userId;

    @Schema(description = "原始自然语言输入")
    @TableField("input_text")
    private String inputText;

    @Schema(description = "解析结果")
    @TableField("parsed_intent")
    private String parsedIntent;

    @Schema(description = "优先级")
    @TableField("priority")
    private Integer priority;

    @Schema(description = "意图状态")
    @TableField("status")
    private String status;

    @Schema(description = "全链路 TraceId")
    @TableField("trace_id")
    private String traceId;

    @TableField("create_time")
    private LocalDateTime createTime;


}
