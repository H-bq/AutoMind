package com.hs.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "拒绝并要求修改请求")
public class ApprovalRejectWithModificationDTO {

    @Schema(description = "拒绝原因", example = "QoS 策略需要降低带宽")
    private String reason;

    @Schema(description = "修改后的载荷 JSON", example = "{\"bandwidthMbps\":100}")
    private Object modifiedPayload;
}
