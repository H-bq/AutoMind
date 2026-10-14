package com.hs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.approval.ApprovalStatus;
import com.hs.agent.dto.ParsedIntent;
import com.hs.common.domain.PageDTO;
import com.hs.common.exception.CommonException;
import com.hs.common.utils.UserContext;
import com.hs.domain.dto.ApprovalQueryDTO;
import com.hs.domain.po.ApprovalRecord;
import com.hs.domain.vo.ApprovalDetailVO;
import com.hs.domain.vo.MyApprovalProgressVO;
import com.hs.domain.vo.ApprovalRecordVO;
import com.hs.mapper.ApprovalRecordMapper;
import com.hs.service.IApprovalRecordService;
import org.springframework.stereotype.Service;

@Service
public class ApprovalRecordServiceImpl extends ServiceImpl<ApprovalRecordMapper, ApprovalRecord> implements IApprovalRecordService {

    private final ObjectMapper objectMapper;

    public ApprovalRecordServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public PageDTO<ApprovalRecordVO> pageApprovals(ApprovalQueryDTO query, boolean pendingOnly) {
        String tenantId = UserContext.requireTenantId();
        Page<ApprovalRecord> page = query.toMpPage("submitted_at", false);
        LambdaQueryWrapper<ApprovalRecord> wrapper = tenantWrapper(tenantId)
                .eq(pendingOnly, ApprovalRecord::getStatus, ApprovalStatus.PENDING.name())
                .eq(!pendingOnly && hasText(query.getStatus()), ApprovalRecord::getStatus, query.getStatus())
                .eq(hasText(query.getApprovalType()), ApprovalRecord::getApprovalType, query.getApprovalType())
                .eq(hasText(query.getRiskLevel()), ApprovalRecord::getRiskLevel, query.getRiskLevel())
                .eq(hasText(query.getTraceId()), ApprovalRecord::getTraceId, query.getTraceId())
                .ge(query.getStartTime() != null, ApprovalRecord::getSubmittedAt, query.getStartTime())
                .le(query.getEndTime() != null, ApprovalRecord::getSubmittedAt, query.getEndTime());
        return PageDTO.of(page(page, wrapper), this::toVO);
    }

    @Override
    public PageDTO<MyApprovalProgressVO> pageMyProgress(ApprovalQueryDTO query) {
        String tenantId = UserContext.requireTenantId();
        Integer userId = UserContext.getUser();
        Page<MyApprovalProgressVO> page = query.toMpPage(null, false);
        return PageDTO.of(baseMapper.selectMyProgressPage(page, query, tenantId, userId));
    }

    @Override
    public ApprovalDetailVO detail(Long id) {
        String tenantId = UserContext.requireTenantId();
        ApprovalDetailVO detail = baseMapper.selectDetail(id, tenantId, UserContext.isAdminTenant(tenantId));
        if (detail == null) {
            throw new CommonException("审批记录不存在", 404);
        }
        detail.setParsedIntent(parseParsedIntent(detail.getParsedIntentJson()));
        return detail;
    }

    private LambdaQueryWrapper<ApprovalRecord> tenantWrapper(String tenantId) {
        return new LambdaQueryWrapper<ApprovalRecord>()
                .eq(!UserContext.isAdminTenant(tenantId), ApprovalRecord::getTenantId, tenantId);
    }

    public ApprovalRecordVO toVO(ApprovalRecord record) {
        if (record == null) {
            return null;
        }
        return new ApprovalRecordVO()
                .setId(record.getId())
                .setTenantId(record.getTenantId())
                .setIntentId(record.getIntentId())
                .setAgentTaskId(record.getAgentTaskId())
                .setTraceId(record.getTraceId())
                .setApproverId(record.getApproverId())
                .setApprovalType(record.getApprovalType())
                .setRiskLevel(record.getRiskLevel())
                .setAiRecommendation(record.getAiRecommendation())
                .setSubmittedPayload(record.getSubmittedPayload())
                .setModifiedPayload(record.getModifiedPayload())
                .setStatus(record.getStatus())
                .setDecisionReason(record.getDecisionReason())
                .setSubmittedAt(record.getSubmittedAt())
                .setDecidedAt(record.getDecidedAt())
                .setExpireAt(record.getExpireAt());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private ParsedIntent parseParsedIntent(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return objectMapper.readValue(value, ParsedIntent.class);
        } catch (JsonProcessingException e) {
            throw new CommonException("审批详情意图解析数据格式错误", 500);
        }
    }
}
