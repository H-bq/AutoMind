package com.hs.common.trace;

import com.hs.common.exception.BadRequestException;
import com.hs.common.exception.ForbiddenException;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.Person;
import com.hs.domain.po.User;
import com.hs.security.JwtClaims;
import com.hs.security.JwtTokenService;
import com.hs.service.IPersonService;
import com.hs.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    private static final String INTENT_HEADER = "X-Intent-Id";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TENANT_HEADER = "X-Tenant-Id";
    private static final String ROLE_HEADER = "X-Role";
    private static final String ACTOR_HEADER = "X-Actor-Id";

    private final String headerName;
    private final IUserService userService;
    private final IPersonService personService;
    private final JwtTokenService jwtTokenService;

    public TraceIdInterceptor(@Value("${automind.trace.header-name:X-Trace-Id}") String headerName,
                              IUserService userService,
                              IPersonService personService,
                              JwtTokenService jwtTokenService) {
        this.headerName = headerName;
        this.userService = userService;
        this.personService = personService;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = request.getHeader(headerName);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        TraceIdHolder.set(traceId);
        response.setHeader(headerName, traceId);

        Integer intentId = parseInteger(request.getHeader(INTENT_HEADER));

        if (isInternalAgentEndpoint(request)) {
            UserContext.setUserInfo(parseInteger(request.getHeader(ACTOR_HEADER)),
                    request.getHeader(TENANT_HEADER), request.getHeader(ROLE_HEADER), intentId);
        } else if (isInternalSlaEndpoint(request) || isRealtimeSlaEndpoint(request)) {
            UserContext.setUserInfo(null, null, null, intentId);
        } else if (!isPublicEndpoint(request)) {
            try {
                JwtClaims claims = jwtTokenService.parseBearerToken(request.getHeader(AUTHORIZATION_HEADER));
                // 先填充上下文，以防校验抛出异常时导致 AuditLog 读取不到租户信息而记录 null
                UserContext.setUserInfo(claims.userId(), claims.tenantId(), claims.role(), intentId);
                validateUserContext(claims.userId(), claims.tenantId(), claims.role());
            } catch (ForbiddenException e) {
                // 抛出给 TenantViolationAdvice 处理，由其负责在使用完后清理 ThreadLocal
                throw e;
            } catch (Exception e) {
                // 其它非 Forbidden 异常，直接清理并重抛，防止 ThreadLocal 泄露
                UserContext.removeUser();
                TraceIdHolder.clear();
                throw e;
            }
        } else {
            UserContext.setUserInfo(null, null, null, intentId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TraceIdHolder.clear();
        UserContext.removeUser();
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("请求头数值格式错误", e);
        }
    }

    private void validateUserContext(Integer userId, String tenantId, String role) {
        if (userId == null) {
            throw new ForbiddenException("缺少用户上下文");
        }
        if (tenantId == null) {
            throw new ForbiddenException("缺少租户上下文");
        }
        if (role == null) {
            throw new ForbiddenException("缺少角色上下文");
        }

        User user = userService.lambdaQuery()
                .eq(User::getUserId, userId)
                .one();
        if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())
                || !role.equalsIgnoreCase(user.getIdentity())) {
            throw new ForbiddenException("用户上下文校验失败");
        }

        Person person = personService.lambdaQuery()
                .eq(Person::getUserId, userId)
                .eq(Person::getTenantId, tenantId)
                .one();
        if (person == null) {
            throw new ForbiddenException("租户上下文校验失败");
        }
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        String path = request.getRequestURI();
        return "/user/login".equals(path)
                || "/user/register".equals(path)
                || "/error".equals(path)
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars");
    }

    private boolean isInternalAgentEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/mcp/tools/")
                || "/a2a/inbox".equals(path);
    }

    private boolean isInternalSlaEndpoint(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/internal/sla-events");
    }

    private boolean isRealtimeSlaEndpoint(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/realtime/sla/stream");
    }
}
