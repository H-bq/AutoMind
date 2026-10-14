package com.hs.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "自愈记录查询参数")
public class HealingRecordQueryDTO extends PageQueryDTO {

    @Schema(description = "触发来源", example = "MCP_ERROR")
    private String triggerSource;

    @Schema(description = "自愈结果", example = "SUCCESS")
    private String result;

    @Schema(description = "TraceId")
    private String traceId;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
