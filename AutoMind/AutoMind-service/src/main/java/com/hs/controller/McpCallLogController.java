package com.hs.controller;

import com.hs.common.domain.PageDTO;
import com.hs.common.domain.R;
import com.hs.domain.dto.McpCallLogQueryDTO;
import com.hs.domain.vo.McpCallLogVO;
import com.hs.service.IMcpCallLogService;
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

@Tag(name = "MCP 调用审计", description = "MCP 调用日志查询接口")
@RestController
@RequestMapping("/mcp-call-logs")
@RequiredArgsConstructor
public class McpCallLogController {

    private final IMcpCallLogService mcpCallLogService;

    @Operation(summary = "分页查询 MCP 调用日志", description = "按 traceId、agentTaskId、工具名、错误类型、风险标记和时间窗过滤")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = PageDTO.class)))
    @GetMapping
    public R<PageDTO<McpCallLogVO>> list(@ParameterObject @ModelAttribute McpCallLogQueryDTO query) {
        return R.ok(mcpCallLogService.pageLogs(query));
    }

    @Operation(summary = "查询 MCP 调用日志详情")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = McpCallLogVO.class)))
    @GetMapping("/{id}")
    public R<McpCallLogVO> detail(
            @Parameter(description = "MCP 调用日志 ID", required = true)
            @PathVariable("id") Long id) {
        return R.ok(mcpCallLogService.detail(id));
    }

    @Operation(summary = "按 traceId 查询 MCP 调用日志")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = McpCallLogVO.class)))
    @GetMapping("/by-trace/{traceId}")
    public R<List<McpCallLogVO>> byTrace(
            @Parameter(description = "全链路 TraceId", required = true)
            @PathVariable("traceId") String traceId) {
        return R.ok(mcpCallLogService.byTrace(traceId));
    }
}
