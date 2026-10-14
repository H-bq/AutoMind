package com.hs.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "意图提交请求")
public class IntentSubmitDTO {

    @Schema(description = "自然语言意图内容", example = "明天早上 8 点到 10 点，从北京到上海开 200Mbps 的视频会议专线，时延不超过 40ms")
    private String inputText;

    @Schema(description = "业务优先级，支持 P1/P2/P3 或数字", example = "P1")
    private String priority;
}
