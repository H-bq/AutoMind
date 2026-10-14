package com.hs.mcp.server.handler;

import com.hs.common.utils.UserContext;
import com.hs.domain.po.AuditLog;
import com.hs.service.IAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BlockFlowToolHandler {

    private final IAuditLogService auditLogService;
    private final McpFaultInjector faultInjector;

    public ResponseEntity<Map<String, Object>> handle(Map<String, Object> params) {
        ResponseEntity<Map<String, Object>> injected = faultInjector.maybeFail("block_flow");
        if (injected != null) {
            return injected;
        }
        auditLogService.save(new AuditLog()
                .setTenantId(UserContext.tenantId())
                .setActorId(UserContext.getUser())
                .setActorRole(UserContext.role())
                .setAction("MCP_BLOCK_FLOW")
                .setCreatedAt(LocalDateTime.now()));
        return ResponseEntity.ok(Map.of(
                "status", "AUDIT_ONLY",
                "flow", params
        ));
    }
}
