package com.hs.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "A2A 消息分页查询参数")
public class A2aMessageQueryDTO extends PageQueryDTO {

    @Schema(description = "TraceId", example = "7d9f...")
    private String traceId;

    @Schema(description = "源网络域 ID")
    private String sourceDomainId;

    @Schema(description = "目标网络域 ID")
    private String targetDomainId;

    @Schema(description = "消息类型", example = "REQ")
    private String messageType;

    @Schema(description = "消息状态", example = "TIMEOUT")
    private String status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
