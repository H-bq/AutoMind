package com.hs.agent.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private final TopologyQueryTool topologyQueryTool;
    private final TelemetryQueryTool telemetryQueryTool;
    private final SlaTargetQueryTool slaTargetQueryTool;
    private final SlaEvaluateTool slaEvaluateTool;
    private final McpDelegatingTool mcpDelegatingTool;
    private final CrossDomainQueryTool crossDomainQueryTool;

    public Object[] toolsFor(String agentRole) {
        return switch (agentRole) {
            case "PLANNER" -> new Object[]{topologyQueryTool, telemetryQueryTool, slaTargetQueryTool, crossDomainQueryTool};
            case "EXECUTOR" -> new Object[]{mcpDelegatingTool};
            case "MONITOR" -> new Object[]{telemetryQueryTool, slaEvaluateTool};
            default -> new Object[0];
        };
    }
}
