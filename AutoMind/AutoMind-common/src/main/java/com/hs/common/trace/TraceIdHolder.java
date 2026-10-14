package com.hs.common.trace;

import org.slf4j.MDC;

public final class TraceIdHolder {

    public static final String TRACE_ID_KEY = "traceId";

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    private TraceIdHolder() {
    }

    public static String get() {
        String traceId = TRACE_ID.get();
        return traceId != null ? traceId : MDC.get(TRACE_ID_KEY);
    }

    public static void set(String traceId) {
        TRACE_ID.set(traceId);
        MDC.put(TRACE_ID_KEY, traceId);
    }

    public static void clear() {
        TRACE_ID.remove();
        MDC.remove(TRACE_ID_KEY);
    }
}
