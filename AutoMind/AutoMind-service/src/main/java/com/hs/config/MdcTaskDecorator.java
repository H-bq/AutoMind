package com.hs.config;

import com.hs.common.trace.TraceIdHolder;
import com.hs.common.utils.UserContext;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String traceId = TraceIdHolder.get();
        UserContext.UserInfo userInfo = UserContext.getUserInfo();
        UserContext.UserInfo copiedUserInfo = userInfo == null ? null :
                new UserContext.UserInfo(userInfo.getUserId(), userInfo.getTenantId(), userInfo.getRole(), userInfo.getIntentId());
        return () -> {
            try {
                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                }
                if (traceId != null) {
                    TraceIdHolder.set(traceId);
                }
                if (copiedUserInfo != null) {
                    UserContext.setUserInfo(copiedUserInfo.getUserId(), copiedUserInfo.getTenantId(),
                            copiedUserInfo.getRole(), copiedUserInfo.getIntentId());
                }
                runnable.run();
            } finally {
                MDC.clear();
                TraceIdHolder.clear();
                UserContext.removeUser();
            }
        };
    }
}
