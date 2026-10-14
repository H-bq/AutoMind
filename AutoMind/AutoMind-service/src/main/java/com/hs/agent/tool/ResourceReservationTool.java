package com.hs.agent.tool;

import com.hs.agent.core.AgentContext;
import com.hs.agent.core.AgentToolContextHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@Deprecated
public class ResourceReservationTool {

    private final McpClient mcpClient;
    private final ObjectMapper objectMapper;

    @Tool(name = "reserveResource", description = "为当前租户指定链路创建网络资源预留记录")
    public String reserve(String linkId,
                          BigDecimal reservedBandwidthMbps,
                          BigDecimal targetLatencyMs,
                          BigDecimal targetPacketLossRate,
                          Integer priority,
                          LocalDateTime effectiveStart,
                          LocalDateTime effectiveEnd) {
        AgentContext context = AgentToolContextHolder.require();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("linkId", linkId);
        params.put("reservedBandwidthMbps", reservedBandwidthMbps);
        params.put("targetLatencyMs", targetLatencyMs);
        params.put("targetPacketLossRate", targetPacketLossRate);
        params.put("priority", priority);
        params.put("effectiveStart", effectiveStart);
        params.put("effectiveEnd", effectiveEnd);
        McpResponse response = mcpClient.invoke(McpCallContext.of(context, "set_qos", "v1"), params);
        try {
            return objectMapper.writeValueAsString(response.body());
        } catch (JsonProcessingException e) {
            return String.valueOf(response.body());
        }
    }
}
