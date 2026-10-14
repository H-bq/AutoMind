package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@Schema(description = "Agent 任务决策展示摘要")
public class AgentTaskDecisionSummaryVO {

    @Schema(description = "展示标题", example = "路径规划完成")
    private String title;

    @Schema(description = "展示摘要", example = "主链路 bj-sh-video-primary，预计时延 28.6ms，满足 SLA")
    private String summary;

    @Schema(description = "展示状态", example = "success")
    private String status;

    @Schema(description = "关键展示项")
    private List<ItemVO> items = new ArrayList<>();

    @Schema(description = "原始输入 JSON 解析结果")
    private Object rawInput;

    @Schema(description = "原始输出 JSON 解析结果")
    private Object rawOutput;

    @Data
    @Accessors(chain = true)
    @Schema(description = "Agent 任务决策展示项")
    public static class ItemVO {
        private String label;
        private Object value;
        private String unit;
        private String status;
    }
}
