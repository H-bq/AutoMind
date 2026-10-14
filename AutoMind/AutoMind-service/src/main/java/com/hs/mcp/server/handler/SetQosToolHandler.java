package com.hs.mcp.server.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.AuditLog;
import com.hs.domain.po.NetworkLinkResource;
import com.hs.mcp.McpErrorType;
import com.hs.service.IAuditLogService;
import com.hs.service.INetworkLinkResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SetQosToolHandler {

    private final INetworkLinkResourceService linkResourceService;
    private final IAuditLogService auditLogService;
    private final McpFaultInjector faultInjector;

    public ResponseEntity<Map<String, Object>> handle(Map<String, Object> params) {
        ResponseEntity<Map<String, Object>> injected = faultInjector.maybeFail("set_qos");
        if (injected != null) {
            return injected;
        }
        String linkId = stringParam(params, "linkId");
        if (linkId == null || linkId.isBlank()) {
            return error(400, McpErrorType.PARAM, "linkId is required");
        }
        String tenantId = UserContext.requireTenantId();
        NetworkLinkResource resource = linkResourceService.getOne(new LambdaQueryWrapper<NetworkLinkResource>()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkLinkResource::getTenantId, tenantId)
                .eq(NetworkLinkResource::getLinkId, linkId)
                .orderByDesc(NetworkLinkResource::getSampledAt)
                .last("limit 1"));
        if (resource == null) {
            return error(404, McpErrorType.RES_NOT_FOUND, "resource not found");
        }
        BigDecimal bandwidth = decimalParam(params, "reservedBandwidthMbps");
        Integer priority = integerParam(params, "priority");
        resource.setQosProfile(bandwidth == null ? resource.getQosProfile() : "BW_" + bandwidth.stripTrailingZeros().toPlainString())
                .setPriorityQueue(priority == null ? resource.getPriorityQueue() : "P" + priority)
                .setSampledAt(LocalDateTime.now());
        linkResourceService.updateById(resource);
        saveAudit("MCP_SET_QOS");
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("linkId", linkId);
        body.put("qosProfile", resource.getQosProfile());
        body.put("priorityQueue", resource.getPriorityQueue());
        body.put("status", "SUCCESS");
        return ResponseEntity.ok(body);
    }

    private void saveAudit(String action) {
        auditLogService.save(new AuditLog()
                .setTenantId(UserContext.tenantId())
                .setActorId(UserContext.getUser())
                .setActorRole(UserContext.role())
                .setAction(action)
                .setCreatedAt(LocalDateTime.now()));
    }

    private ResponseEntity<Map<String, Object>> error(int status, McpErrorType errorType, String message) {
        return ResponseEntity.status(status).body(Map.of("errorType", errorType.name(), "message", message));
    }

    private String stringParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private BigDecimal decimalParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value == null ? null : new BigDecimal(String.valueOf(value));
    }

    private Integer integerParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value == null ? null : Integer.valueOf(String.valueOf(value));
    }
}
