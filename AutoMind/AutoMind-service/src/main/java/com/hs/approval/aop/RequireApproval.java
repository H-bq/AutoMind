package com.hs.approval.aop;

import com.hs.approval.ApprovalType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireApproval {
    ApprovalType type() default ApprovalType.HIGH_RISK_MCP;
}
