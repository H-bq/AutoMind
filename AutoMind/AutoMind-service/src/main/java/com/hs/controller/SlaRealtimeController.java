package com.hs.controller;

import com.hs.common.utils.UserContext;
import com.hs.realtime.SlaRealtimePublisher;
import com.hs.security.JwtClaims;
import com.hs.security.JwtTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "SlaRealtime", description = "SLA 实时事件推送接口")
@RestController
@RequestMapping("/realtime/sla")
@RequiredArgsConstructor
public class SlaRealtimeController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final SlaRealtimePublisher slaRealtimePublisher;

    @Operation(summary = "订阅 SLA 实时事件流", description = "前端 EventSource 使用 token 参数订阅 SSE")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @Parameter(description = "JWT token，可带 Bearer 前缀", required = true)
            @RequestParam("token") String token) {
        JwtClaims claims = jwtTokenService.parseBearerToken(toBearer(token));
        boolean admin = UserContext.isAdminTenant(claims.tenantId());
        return slaRealtimePublisher.subscribe(claims.tenantId(), admin);
    }

    private String toBearer(String token) {
        if (token == null) {
            return null;
        }
        String value = token.trim();
        return value.startsWith(BEARER_PREFIX) ? value : BEARER_PREFIX + value;
    }
}
