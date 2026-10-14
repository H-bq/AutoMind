package com.hs.agent.core;

public record AgentRunResult<O>(AgentContext context, O output) {
}
