package com.hs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hs.common.domain.PageDTO;
import com.hs.common.exception.CommonException;
import com.hs.common.utils.UserContext;
import com.hs.domain.dto.HealingRecordQueryDTO;
import com.hs.domain.po.HealingRecord;
import com.hs.domain.po.IntentRequest;
import com.hs.domain.vo.HealingRecordVO;
import com.hs.mapper.HealingRecordMapper;
import com.hs.service.IHealingRecordService;
import com.hs.service.IIntentRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HealingRecordServiceImpl extends ServiceImpl<HealingRecordMapper, HealingRecord> implements IHealingRecordService {

    private final IIntentRequestService intentRequestService;

    @Override
    public PageDTO<HealingRecordVO> pageRecords(HealingRecordQueryDTO query) {
        String tenantId = UserContext.requireTenantId();
        Page<HealingRecord> page = query.toMpPage("created_at", false);
        LambdaQueryWrapper<HealingRecord> wrapper = tenantWrapper(tenantId)
                .eq(hasText(query.getTriggerSource()), HealingRecord::getTriggerSource, query.getTriggerSource())
                .eq(hasText(query.getResult()), HealingRecord::getResult, query.getResult())
                .eq(hasText(query.getTraceId()), HealingRecord::getTraceId, query.getTraceId())
                .ge(query.getStartTime() != null, HealingRecord::getCreatedAt, query.getStartTime())
                .le(query.getEndTime() != null, HealingRecord::getCreatedAt, query.getEndTime());
        return PageDTO.of(page(page, wrapper), this::toVO);
    }

    @Override
    public HealingRecordVO detail(Long id) {
        String tenantId = UserContext.requireTenantId();
        HealingRecord record = lambdaQuery()
                .eq(HealingRecord::getId, id)
                .eq(!UserContext.isAdminTenant(tenantId), HealingRecord::getTenantId, tenantId)
                .one();
        if (record == null) {
            throw new CommonException("自愈记录不存在", 404);
        }
        return toVO(record);
    }

    @Override
    public List<HealingRecordVO> byIntent(Integer intentId) {
        String tenantId = UserContext.requireTenantId();
        IntentRequest intent = intentRequestService.getById(intentId);
        if (intent == null || (!UserContext.isAdminTenant(tenantId) && !tenantId.equals(intent.getTenantId()))) {
            throw new CommonException("意图请求不存在", 404);
        }
        return lambdaQuery()
                .eq(HealingRecord::getIntentId, intentId)
                .eq(!UserContext.isAdminTenant(tenantId), HealingRecord::getTenantId, tenantId)
                .orderByAsc(HealingRecord::getCreatedAt)
                .list().stream().map(this::toVO).toList();
    }

    private LambdaQueryWrapper<HealingRecord> tenantWrapper(String tenantId) {
        return new LambdaQueryWrapper<HealingRecord>()
                .eq(!UserContext.isAdminTenant(tenantId), HealingRecord::getTenantId, tenantId);
    }

    private HealingRecordVO toVO(HealingRecord record) {
        if (record == null) {
            return null;
        }
        return new HealingRecordVO()
                .setId(record.getId())
                .setTenantId(record.getTenantId())
                .setIntentId(record.getIntentId())
                .setTriggerAgentTaskId(record.getTriggerAgentTaskId())
                .setTraceId(record.getTraceId())
                .setTriggerSource(record.getTriggerSource())
                .setRootCause(record.getRootCause())
                .setAttemptNo(record.getAttemptNo())
                .setMaxAttempts(record.getMaxAttempts())
                .setStrategy(record.getStrategy())
                .setResult(record.getResult())
                .setRecoveredAt(record.getRecoveredAt())
                .setCreatedAt(record.getCreatedAt());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
