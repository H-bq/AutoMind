package com.hs.mcp.server;

import com.hs.mcp.McpErrorType;
import com.hs.mcp.server.handler.BlockFlowToolHandler;
import com.hs.mcp.server.handler.GetTopoToolHandler;
import com.hs.mcp.server.handler.SetQosToolHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Mock MCP Server", description = "阶段三同进程 MCP 工具端点")
@RestController
@RequestMapping("/mcp/tools")
@RequiredArgsConstructor
public class MockMcpServerController {

    private final GetTopoToolHandler getTopoToolHandler;
    private final SetQosToolHandler setQosToolHandler;
    private final BlockFlowToolHandler blockFlowToolHandler;

    @Operation(summary = "调用 Mock MCP 工具", description = "同进程模拟 MCP Server，供 McpClient 审计调用")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @PostMapping("/{name}/invoke")
    public ResponseEntity<Map<String, Object>> invoke(
            @Parameter(description = "工具名", required = true)
            @PathVariable("name") String name,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(value = "X-Agent-Task-Id", required = false) String agentTaskId,
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestBody Map<String, Object> body) {
        if (isBlank(traceId) || isBlank(tenantId) || isBlank(role)) {
            return error(400, McpErrorType.FORMAT, "missing required MCP headers");
        }
        Object paramsObject = body == null ? null : body.get("params");
        Map<String, Object> params = paramsObject instanceof Map<?, ?> map
                ? map.entrySet().stream().collect(java.util.stream.Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue))
                : Map.of();
        return switch (name) {
            case "get_topo" -> getTopoToolHandler.handle(params);
            case "set_qos" -> setQosToolHandler.handle(params);
            case "block_flow" -> blockFlowToolHandler.handle(params);
            default -> error(404, McpErrorType.TOOL_NOT_FOUND, "tool not found");
        };
    }

    private ResponseEntity<Map<String, Object>> error(int status, McpErrorType errorType, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "errorType", errorType.name(),
                "message", message
        ));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
