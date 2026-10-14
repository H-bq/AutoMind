package com.hs.mcp;

public enum McpErrorType {
    FORMAT(false),
    PARAM(false),
    TOOL_NOT_FOUND(false),
    RES_NOT_FOUND(false),
    RES_CONFLICT(true),
    PERMISSION(false),
    TIMEOUT(true),
    CAPACITY(false),
    INTERNAL(false);

    private final boolean retryable;

    McpErrorType(boolean retryable) {
        this.retryable = retryable;
    }

    public boolean retryable() {
        return retryable;
    }
}
