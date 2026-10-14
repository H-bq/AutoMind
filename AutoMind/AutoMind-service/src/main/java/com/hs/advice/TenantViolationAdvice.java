package com.hs.advice;

import com.hs.common.domain.R;
import com.hs.common.exception.ForbiddenException;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.AuditLog;
import com.hs.service.IAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class TenantViolationAdvice {

    private final IAuditLogService auditLogService;

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<R<Void>> handleForbidden(ForbiddenException e) {
        try {
            auditLogService.save(new AuditLog()
                    .setTenantId(UserContext.tenantId())
                    .setActorId(UserContext.getUser())
                    .setActorRole(UserContext.role())
                    .setAction("TENANT_VIOLATION")
                    .setCreatedAt(LocalDateTime.now()));
        } catch (Exception auditException) {
            log.error("写入租户违规审计失败", auditException);
        } finally {
            // 清理 ThreadLocal 避免内存泄漏
            UserContext.removeUser();
            com.hs.common.trace.TraceIdHolder.clear();
        }
        log.error("权限异常 -> {}", e.getMessage());
        return ResponseEntity.status(e.getCode()).body(R.error(e));
    }
}
