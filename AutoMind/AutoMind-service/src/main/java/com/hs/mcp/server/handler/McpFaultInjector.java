package com.hs.mcp.server.handler;

import com.hs.mcp.McpErrorType;
import com.hs.mcp.config.AutomindMcpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpFaultInjector {

    private final AutomindMcpProperties properties;

    public ResponseEntity<Map<String, Object>> maybeFail(String toolName) {
        Map<String, Integer> toolFaults = properties.getFaultInjection().get(toolName);
        if (toolFaults == null || toolFaults.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, Integer> entry : toolFaults.entrySet()) {
            int percent = entry.getValue() == null ? 0 : entry.getValue();
            if (percent > 0 && ThreadLocalRandom.current().nextInt(100) < percent) {
                McpErrorType errorType = McpErrorType.valueOf(entry.getKey());
                log.warn("MCP fault injected tool={}, errorType={}", toolName, errorType);
                return ResponseEntity.status(status(errorType)).body(Map.of(
                        "errorType", errorType.name(),
                        "message", "fault injected"
                ));
            }
        }
        return null;
    }

    private int status(McpErrorType errorType) {
        return switch (errorType) {
            case FORMAT, PARAM -> 400;
            case TOOL_NOT_FOUND, RES_NOT_FOUND -> 404;
            case RES_CONFLICT -> 409;
            case PERMISSION -> 403;
            case TIMEOUT -> 504;
            case CAPACITY -> 503;
            case INTERNAL -> 500;
        };
    }
}
