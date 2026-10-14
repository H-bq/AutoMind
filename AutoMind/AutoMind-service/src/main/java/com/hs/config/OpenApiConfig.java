package com.hs.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.Collections;
import java.util.Set;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(BEARER_AUTH,
                        new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    @Bean
    public OperationCustomizer publicEndpointCustomizer() {
        Set<String> publicMethods = Set.of("login", "register");
        return (operation, handlerMethod) -> {
            if (isPublicUserOperation(handlerMethod, publicMethods)) {
                operation.setSecurity(Collections.emptyList());
            }
            return operation;
        };
    }

    private boolean isPublicUserOperation(HandlerMethod handlerMethod, Set<String> publicMethods) {
        return "UserController".equals(handlerMethod.getBeanType().getSimpleName())
                && publicMethods.contains(handlerMethod.getMethod().getName());
    }
}
