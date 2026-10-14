package com.hs.simulator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "异常遥测生成请求")
public class AnomalyGenerateDTO {

    @Schema(description = "指定链路 ID；为空时对全部链路生成异常数据")
    private String linkId;

    @Schema(description = "每条链路插入的异常采样数量；为空时使用配置 automind.simulator.anomaly-burst-samples", example = "30")
    private Integer sampleCount;
}
