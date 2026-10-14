package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@Schema(description = "按意图分组的 Agent 任务视图")
public class AgentTaskGroupVO {

    @Schema(description = "意图 ID", example = "1001")
    private Integer intentId;

    @Schema(description = "租户 ID", example = "tenant-a")
    private String tenantId;

    @Schema(description = "用户职位", example = "网络运维工程师")
    private String position;

    @Schema(description = "用户原始请求文本", example = "为北京到上海链路扩容")
    private String inputText;

    @Schema(description = "意图请求创建时间")
    private LocalDateTime createTime;

    @Schema(description = "该意图下的 Agent 任务列表，前端展开分组时展示")
    private List<AgentTaskVO> agentTasks = new ArrayList<>();
}
