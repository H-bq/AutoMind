package com.hs.mcp;

import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;

@Component
public class McpErrorClassifier {

    public McpErrorType classifyHttp(int httpStatus, String body) {
        if (httpStatus >= 200 && httpStatus < 300) {
            return null;
        }
        return switch (httpStatus) {
            case 400 -> body != null && body.toLowerCase().contains("schema") ? McpErrorType.FORMAT : McpErrorType.PARAM;
            case 401, 403 -> McpErrorType.PERMISSION;
            case 404 -> body != null && body.toLowerCase().contains("resource") ? McpErrorType.RES_NOT_FOUND : McpErrorType.TOOL_NOT_FOUND;
            case 408, 504 -> McpErrorType.TIMEOUT;
            case 409 -> McpErrorType.RES_CONFLICT;
            case 429, 503 -> McpErrorType.CAPACITY;
            default -> httpStatus >= 500 ? McpErrorType.INTERNAL : McpErrorType.PARAM;
        };
    }

    public McpErrorType classifyException(Throwable e) {
        if (e instanceof ResourceAccessException || hasCause(e, SocketTimeoutException.class)) {
            return McpErrorType.TIMEOUT;
        }
        if (e instanceof RestClientException) {
            return McpErrorType.INTERNAL;
        }
        return McpErrorType.INTERNAL;
    }

    private boolean hasCause(Throwable e, Class<? extends Throwable> causeType) {
        Throwable current = e;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
