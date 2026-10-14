package com.hs.mcp;

import com.hs.agent.core.AgentContext;

public record McpCallContext(
        AgentContext agent,
        String toolName,
        String toolVersion,
        int attemptNo,
        boolean dryRun
) {

    public static McpCallContext of(AgentContext agent, String toolName, String toolVersion) {
        return new McpCallContext(agent, toolName, toolVersion, 1, false);
    }

    public McpCallContext withAttemptNo(int attemptNo) {
        return new McpCallContext(agent, toolName, toolVersion, attemptNo, dryRun);
    }
}
