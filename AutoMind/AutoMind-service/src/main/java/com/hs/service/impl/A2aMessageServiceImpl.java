package com.hs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hs.common.domain.PageDTO;
import com.hs.common.exception.CommonException;
import com.hs.common.exception.ForbiddenException;
import com.hs.common.utils.UserContext;
import com.hs.domain.dto.A2aMessageQueryDTO;
import com.hs.domain.po.A2aMessagePO;
import com.hs.domain.vo.A2aMessageVO;
import com.hs.mapper.A2aMessageMapper;
import com.hs.service.IA2aMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class A2aMessageServiceImpl extends ServiceImpl<A2aMessageMapper, A2aMessagePO> implements IA2aMessageService {

    @Override
    public PageDTO<A2aMessageVO> pageMessages(A2aMessageQueryDTO query) {
        requireAuditRole();
        String tenantId = UserContext.requireTenantId();
        Page<A2aMessagePO> page = query.toMpPage("sent_at", false);
        LambdaQueryWrapper<A2aMessagePO> wrapper = baseWrapper(tenantId)
                .eq(hasText(query.getTraceId()), A2aMessagePO::getTraceId, query.getTraceId())
                .eq(hasText(query.getSourceDomainId()), A2aMessagePO::getSourceDomainId, query.getSourceDomainId())
                .eq(hasText(query.getTargetDomainId()), A2aMessagePO::getTargetDomainId, query.getTargetDomainId())
                .eq(hasText(query.getMessageType()), A2aMessagePO::getMessageType, query.getMessageType())
                .eq(hasText(query.getStatus()), A2aMessagePO::getStatus, query.getStatus())
                .ge(query.getStartTime() != null, A2aMessagePO::getSentAt, query.getStartTime())
                .le(query.getEndTime() != null, A2aMessagePO::getSentAt, query.getEndTime());
        return PageDTO.of(page(page, wrapper), this::toVO);
    }

    @Override
    public A2aMessageVO detail(Long id) {
        requireAuditRole();
        String tenantId = UserContext.requireTenantId();
        A2aMessagePO message = lambdaQuery()
                .eq(A2aMessagePO::getId, id)
                .eq(!UserContext.isAdminTenant(tenantId), A2aMessagePO::getTenantId, tenantId)
                .one();
        if (message == null) {
            throw new CommonException("A2A 消息不存在", 404);
        }
        return toVO(message);
    }

    @Override
    public List<A2aMessageVO> byTrace(String traceId) {
        requireAuditRole();
        String tenantId = UserContext.requireTenantId();
        return lambdaQuery()
                .eq(A2aMessagePO::getTraceId, traceId)
                .eq(!UserContext.isAdminTenant(tenantId), A2aMessagePO::getTenantId, tenantId)
                .orderByAsc(A2aMessagePO::getSentAt)
                .list().stream().map(this::toVO).toList();
    }

    private LambdaQueryWrapper<A2aMessagePO> baseWrapper(String tenantId) {
        return new LambdaQueryWrapper<A2aMessagePO>()
                .eq(!UserContext.isAdminTenant(tenantId), A2aMessagePO::getTenantId, tenantId);
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

    private A2aMessageVO toVO(A2aMessagePO message) {
        return new A2aMessageVO()
                .setId(message.getId())
                .setTenantId(message.getTenantId())
                .setSourceDomainId(message.getSourceDomainId())
                .setTargetDomainId(message.getTargetDomainId())
                .setSourceAgent(message.getSourceAgent())
                .setTargetAgent(message.getTargetAgent())
                .setTaskId(message.getTaskId())
                .setTraceId(message.getTraceId())
                .setMessageType(message.getMessageType())
                .setPayload(message.getPayload())
                .setStatus(message.getStatus())
                .setLatencyMs(message.getLatencyMs())
                .setSentAt(message.getSentAt())
                .setReceivedAt(message.getReceivedAt());
    }
}
