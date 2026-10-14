package com.hs.mcp.exception;

import com.hs.common.exception.CommonException;
import com.hs.mcp.McpCallContext;
import com.hs.mcp.McpErrorType;
import com.hs.mcp.McpResponse;
import lombok.Getter;

@Getter
public class McpException extends CommonException {

    private final McpErrorType errorType;
    private final McpCallContext context;
    private final McpResponse response;

    public McpException(McpErrorType errorType, McpCallContext context, McpResponse response, String message) {
        super(message, 502);
        this.errorType = errorType;
        this.context = context;
        this.response = response;
    }

    public McpException(McpErrorType errorType, McpCallContext context, McpResponse response, String message, Throwable cause) {
        super(message, cause, 502);
        this.errorType = errorType;
        this.context = context;
        this.response = response;
    }
}
