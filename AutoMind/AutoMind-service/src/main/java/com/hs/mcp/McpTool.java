package com.hs.mcp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpTool {
    String name();

    String version() default "v1";

    boolean highRisk() default false;

    String description() default "";

    String[] requiredRoles() default {"ADMIN", "OPS", "OPERATOR"};
}
