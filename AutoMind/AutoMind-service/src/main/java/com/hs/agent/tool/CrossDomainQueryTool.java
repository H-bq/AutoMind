package com.hs.agent.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.a2a.A2aFallbackGuard;
import com.hs.a2a.A2aMessage;
import com.hs.a2a.A2aMessageType;
import com.hs.a2a.config.AutomindA2aProperties;
import com.hs.agent.core.AgentContext;
import com.hs.agent.core.AgentToolContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CrossDomainQueryTool {

    private final A2aFallbackGuard fallbackGuard;
    private final AutomindA2aProperties properties;
    private final ObjectMapper objectMapper;

    @Tool(name = "queryCrossDomainLinkStatus", description = "通过 A2A 查询外域链路状态；失败时返回本域降级标记")
    public String queryCrossDomainLinkStatus(String sourceDomainId, String targetDomainId, String linkId) {
        AgentContext context = AgentToolContextHolder.require();
        A2aMessage request = new A2aMessage(
                context.tenantId(),
                sourceDomainId,
                targetDomainId,
                "PLANNER",
                "PLANNER",
                context.agentTaskId(),
                context.traceId(),
                A2aMessageType.REQ,
                Map.of("linkId", linkId == null ? "" : linkId)
        );
        Map<String, Object> result = fallbackGuard.sendOrFallback(
                request,
                Duration.ofMillis(properties.getTimeoutMs()),
                response -> {
                    Map<String, Object> payload = new java.util.LinkedHashMap<>(response.payload());
                    payload.put("crossDomainDegraded", false);
                    payload.put("targetDomainId", targetDomainId == null ? "" : targetDomainId);
                    return payload;
                },
                () -> Map.of("crossDomainDegraded", true, "links", java.util.List.of())
        );
        return toJson(result);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
