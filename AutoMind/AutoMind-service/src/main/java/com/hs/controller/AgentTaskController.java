package com.hs.controller;

import com.hs.common.domain.PageDTO;
import com.hs.common.domain.R;
import com.hs.domain.dto.AgentTaskQueryDTO;
import com.hs.domain.vo.AgentExecutionTimelineVO;
import com.hs.domain.vo.AgentTaskGroupVO;
import com.hs.domain.vo.AgentTaskVO;
import com.hs.service.IAgentTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AgentTask", description = "Agent 任务与 PEV 轨迹接口")
@RestController
@RequestMapping("/agent-tasks")
@RequiredArgsConstructor
public class AgentTaskController {

    private final IAgentTaskService agentTaskService;

    @Operation(summary = "分页查询按意图分组的 Agent 任务", description = "普通租户仅返回本租户任务，admin 返回全量")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = PageDTO.class)))
    @GetMapping
    public R<PageDTO<AgentTaskGroupVO>> list(@ParameterObject @ModelAttribute AgentTaskQueryDTO query) {
        return R.ok(agentTaskService.pageTasks(query));
    }

//    @Operation(summary = "查询 Agent 任务详情")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = AgentTaskVO.class)))
    @GetMapping("/{id}")
    public R<AgentTaskVO> detail(
            @Parameter(description = "Agent 任务 ID", required = true)
            @PathVariable("id") Long id) {
        return R.ok(agentTaskService.detail(id));
    }

//    @Operation(summary = "按 traceId 查询 Agent 全链路任务")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = AgentTaskVO.class)))
    @GetMapping("/by-trace/{traceId}")
    public R<List<AgentTaskVO>> byTrace(
            @Parameter(description = "全链路 TraceId", required = true)
            @PathVariable("traceId") String traceId) {
        return R.ok(agentTaskService.byTrace(traceId));
    }

    @Operation(summary = "按 intentId 查询 Agent 任务")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = AgentTaskVO.class)))
    @GetMapping("/by-intent/{intentId}")
    public R<List<AgentTaskVO>> byIntent(
            @Parameter(description = "意图 ID", required = true)
            @PathVariable("intentId") Integer intentId) {
        return R.ok(agentTaskService.byIntent(intentId));
    }

    @Operation(summary = "按 intentId 查询 Agent 执行时间线", description = "聚合 Agent 任务、MCP 调用、A2A 消息、审批和 SLA 记录，返回前端可直接展示的时间线")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = AgentExecutionTimelineVO.class)))
    @GetMapping("/timeline/by-intent/{intentId}")
    public R<AgentExecutionTimelineVO> timelineByIntent(
            @Parameter(description = "意图 ID", required = true)
            @PathVariable("intentId") Integer intentId) {
        return R.ok(agentTaskService.timelineByIntent(intentId));
    }

    @Operation(summary = "按 traceId 查询 Agent 执行时间线", description = "聚合 Agent 任务、MCP 调用、A2A 消息、审批和 SLA 记录，返回前端可直接展示的时间线")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = AgentExecutionTimelineVO.class)))
    @GetMapping("/timeline/by-trace/{traceId}")
    public R<AgentExecutionTimelineVO> timelineByTrace(
            @Parameter(description = "全链路 TraceId", required = true)
            @PathVariable("traceId") String traceId) {
        return R.ok(agentTaskService.timelineByTrace(traceId));
    }
}
