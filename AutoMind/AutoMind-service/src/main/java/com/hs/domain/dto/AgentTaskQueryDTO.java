package com.hs.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Agent 任务分页查询参数")
public class AgentTaskQueryDTO extends PageQueryDTO {

    @Schema(description = "PEV 状态", example = "COMPLETED")
    private String pevState;

    @Schema(description = "Agent 角色", example = "PLANNER")
    private String agentRole;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
