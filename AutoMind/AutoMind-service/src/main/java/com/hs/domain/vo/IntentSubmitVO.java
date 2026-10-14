package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "意图提交响应")
public class IntentSubmitVO {

    @Schema(description = "意图请求 ID", example = "1")
    private Integer intentId;

    @Schema(description = "全链路 TraceId", example = "trace-20260508-0001")
    private String traceId;
}
