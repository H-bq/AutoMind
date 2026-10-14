package com.hs.mcp.event;

import com.hs.mcp.McpCallContext;
import org.springframework.context.ApplicationEvent;

public class McpTimeoutEvent extends ApplicationEvent {

    private final McpCallContext context;

    public McpTimeoutEvent(Object source, McpCallContext context) {
        super(source);
        this.context = context;
    }

    public McpCallContext context() {
        return context;
    }
}
