package com.hs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hs.common.domain.PageDTO;
import com.hs.common.exception.CommonException;
import com.hs.common.exception.ForbiddenException;
import com.hs.common.utils.UserContext;
import com.hs.domain.dto.McpCallLogQueryDTO;
import com.hs.domain.po.McpCallLog;
import com.hs.domain.vo.McpCallLogVO;
import com.hs.mapper.McpCallLogMapper;
import com.hs.service.IMcpCallLogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class McpCallLogServiceImpl extends ServiceImpl<McpCallLogMapper, McpCallLog> implements IMcpCallLogService {

    @Override
    public PageDTO<McpCallLogVO> pageLogs(McpCallLogQueryDTO query) {
        requireAuditRole();
        String tenantId = UserContext.requireTenantId();
        Page<McpCallLog> page = query.toMpPage("called_at", false);
        LambdaQueryWrapper<McpCallLog> wrapper = baseWrapper(tenantId)
                .eq(hasText(query.getTraceId()), McpCallLog::getTraceId, query.getTraceId())
                .eq(query.getAgentTaskId() != null, McpCallLog::getAgentTaskId, query.getAgentTaskId())
                .eq(hasText(query.getToolName()), McpCallLog::getToolName, query.getToolName())
                .eq(hasText(query.getErrorType()), McpCallLog::getErrorType, query.getErrorType())
                .eq(query.getIsHighRisk() != null, McpCallLog::getIsHighRisk, query.getIsHighRisk())
                .ge(query.getStartTime() != null, McpCallLog::getCalledAt, query.getStartTime())
                .le(query.getEndTime() != null, McpCallLog::getCalledAt, query.getEndTime());
        return PageDTO.of(page(page, wrapper), this::toVO);
    }

    @Override
    public McpCallLogVO detail(Long id) {
        requireAuditRole();
        String tenantId = UserContext.requireTenantId();
        McpCallLog log = lambdaQuery()
                .eq(McpCallLog::getId, id)
                .eq(!UserContext.isAdminTenant(tenantId), McpCallLog::getTenantId, tenantId)
                .one();
        if (log == null) {
            throw new CommonException("MCP 调用日志不存在", 404);
        }
        return toVO(log);
    }

    @Override
    public List<McpCallLogVO> byTrace(String traceId) {
        requireAuditRole();
        String tenantId = UserContext.requireTenantId();
        return lambdaQuery()
                .eq(McpCallLog::getTraceId, traceId)
                .eq(!UserContext.isAdminTenant(tenantId), McpCallLog::getTenantId, tenantId)
                .orderByAsc(McpCallLog::getCalledAt)
                .list().stream().map(this::toVO).toList();
    }

    private LambdaQueryWrapper<McpCallLog> baseWrapper(String tenantId) {
        return new LambdaQueryWrapper<McpCallLog>()
                .eq(!UserContext.isAdminTenant(tenantId), McpCallLog::getTenantId, tenantId);
    }

    private void requireAuditRole() {
        String role = UserContext.requireRole();
        if (!"ADMIN".equalsIgnoreCase(role) && !"AUDITOR".equalsIgnoreCase(role)) {
            throw new ForbiddenException("审计查询仅允许 ADMIN / AUDITOR 角色访问");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private McpCallLogVO toVO(McpCallLog log) {
        return new McpCallLogVO()
                .setId(log.getId())
                .setTenantId(log.getTenantId())
                .setAgentTaskId(log.getAgentTaskId())
                .setIntentId(log.getIntentId())
                .setTraceId(log.getTraceId())
                .setToolName(log.getToolName())
                .setToolVersion(log.getToolVersion())
                .setRequestParams(log.getRequestParams())
                .setResponseCode(log.getResponseCode())
                .setResponseBody(log.getResponseBody())
                .setErrorType(log.getErrorType())
                .setLatencyMs(log.getLatencyMs())
                .setAttemptNo(log.getAttemptNo())
                .setIsHighRisk(log.getIsHighRisk())
                .setCalledAt(log.getCalledAt());
    }
}
