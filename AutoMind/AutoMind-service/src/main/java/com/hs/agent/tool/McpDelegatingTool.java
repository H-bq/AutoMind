package com.hs.agent.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.agent.core.AgentContext;
import com.hs.agent.core.AgentToolContextHolder;
import com.hs.mcp.McpCallContext;
import com.hs.mcp.McpClient;
import com.hs.mcp.McpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class McpDelegatingTool {

    private final McpClient mcpClient;
    private final ObjectMapper objectMapper;

    @Tool(name = "reserveResource", description = "通过 MCP set_qos 为指定链路设置 QoS 资源策略")
    public String reserveResource(String linkId,
                                  BigDecimal reservedBandwidthMbps,
                                  BigDecimal targetLatencyMs,
                                  BigDecimal targetPacketLossRate,
                                  Integer priority,
                                  LocalDateTime effectiveStart,
                                  LocalDateTime effectiveEnd) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("linkId", linkId);
        params.put("reservedBandwidthMbps", reservedBandwidthMbps);
        params.put("targetLatencyMs", targetLatencyMs);
        params.put("targetPacketLossRate", targetPacketLossRate);
        params.put("priority", priority);
        params.put("effectiveStart", effectiveStart);
        params.put("effectiveEnd", effectiveEnd);
        return invoke("set_qos", params);
    }

    @Tool(name = "setQos", description = "通过 MCP 设置链路 QoS 策略")
    public String setQos(String linkId, BigDecimal reservedBandwidthMbps, Integer priority) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("linkId", linkId);
        params.put("reservedBandwidthMbps", reservedBandwidthMbps);
        params.put("priority", priority);
        return invoke("set_qos", params);
    }

    @Tool(name = "blockFlow", description = "通过 MCP 阻断指定流量五元组，阶段三仅写审计")
    public String blockFlow(String sourceIp, String targetIp, Integer sourcePort, Integer targetPort, String protocol) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("sourceIp", sourceIp);
        params.put("targetIp", targetIp);
        params.put("sourcePort", sourcePort);
        params.put("targetPort", targetPort);
        params.put("protocol", protocol);
        return invoke("block_flow", params);
    }

    @Tool(name = "getTopoByMcp", description = "通过 MCP 查询当前租户拓扑")
    public String getTopoByMcp() {
        return invoke("get_topo", Map.of());
    }

    private String invoke(String toolName, Map<String, Object> params) {
        AgentContext agentContext = AgentToolContextHolder.require();
        McpResponse response = mcpClient.invoke(McpCallContext.of(agentContext, toolName, "v1"), params);
        return toJson(response.body());
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
