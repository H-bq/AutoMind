package com.hs.advice;

import com.hs.common.domain.R;
import com.hs.common.trace.TraceIdHolder;
import com.hs.mcp.exception.McpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class McpExceptionAdvice {

    @ExceptionHandler(McpException.class)
    public ResponseEntity<R<Map<String, Object>>> handleMcpException(McpException e) {
        log.error("MCP 调用异常 tool={}, errorType={}",
                e.getContext() == null ? null : e.getContext().toolName(), e.getErrorType());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorType", e.getErrorType() == null ? null : e.getErrorType().name());
        body.put("toolName", e.getContext() == null ? null : e.getContext().toolName());
        body.put("traceId", e.getContext() == null || e.getContext().agent() == null
                ? TraceIdHolder.get()
                : e.getContext().agent().traceId());
        body.put("attemptNo", e.getContext() == null ? null : e.getContext().attemptNo());
        return ResponseEntity.status(502).body(new R<>(502, e.getMessage(), body));
    }
}
