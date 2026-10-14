package com.hs.agent.core;

public final class AgentToolContextHolder {

    private static final ThreadLocal<AgentContext> CONTEXT = new ThreadLocal<>();

    private AgentToolContextHolder() {
    }

    public static void set(AgentContext context) {
        CONTEXT.set(context);
    }

    public static AgentContext require() {
        AgentContext context = CONTEXT.get();
        if (context == null) {
            throw new IllegalStateException("缺少 Agent 工具调用上下文");
        }
        return context;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
