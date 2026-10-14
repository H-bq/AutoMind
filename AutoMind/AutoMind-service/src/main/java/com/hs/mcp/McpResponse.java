package com.hs.mcp;

public record McpResponse(
        int httpStatus,
        McpErrorType errorType,
        Object body,
        long latencyMs
) {
}
