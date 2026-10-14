package com.hs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hs.common.domain.PageDTO;
import com.hs.common.utils.UserContext;
import com.hs.domain.dto.SlaRecordQueryDTO;
import com.hs.domain.po.Person;
import com.hs.domain.po.SlaRecord;
import com.hs.domain.vo.SlaRecordVO;
import com.hs.mapper.SlaRecordMapper;
import com.hs.service.IPersonService;
import com.hs.service.ISlaRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class SlaRecordServiceImpl extends ServiceImpl<SlaRecordMapper, SlaRecord> implements ISlaRecordService {

    @Autowired
    private IPersonService personService;

    @Override
    public PageDTO<SlaRecordVO> records(SlaRecordQueryDTO query) {
        String tenantId = UserContext.requireTenantId();
        Page<SlaRecord> page = query.toMpPage("evaluated_at", false);
        LambdaQueryWrapper<SlaRecord> wrapper = new LambdaQueryWrapper<SlaRecord>()
                .eq(!UserContext.isAdminTenant(tenantId), SlaRecord::getTenantId, tenantId)
                .eq(query.getLinkId() != null && !query.getLinkId().isBlank(), SlaRecord::getLinkId, query.getLinkId())
                .eq(query.getMetricType() != null && !query.getMetricType().isBlank(), SlaRecord::getMetricType, query.getMetricType())
                .eq(query.getBreachFlag() != null, SlaRecord::getBreachFlag, query.getBreachFlag());
        Page<SlaRecord> result = page(page, wrapper);

        List<String> tenantIds = result.getRecords().stream()
                .map(SlaRecord::getTenantId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, String> tenantToPositionMap = new HashMap<>();
        if (!tenantIds.isEmpty()) {
            List<Person> persons = personService.list(new LambdaQueryWrapper<Person>()
                    .in(Person::getTenantId, tenantIds));
            for (Person person : persons) {
                if (person.getTenantId() != null && person.getPosition() != null) {
                    tenantToPositionMap.put(person.getTenantId(), person.getPosition());
                }
            }
        }

        return PageDTO.of(result, record -> {
            SlaRecordVO vo = toSlaRecordVO(record);
            vo.setPosition(tenantToPositionMap.getOrDefault(record.getTenantId(), ""));
            return vo;
        });
    }

    private SlaRecordVO toSlaRecordVO(SlaRecord record) {
        SlaRecordVO vo = new SlaRecordVO();
        vo.setId(record.getId());
        vo.setIntentId(record.getIntentId());
        vo.setLinkId(record.getLinkId());
        vo.setMetricType(record.getMetricType());
        vo.setTargetValue(record.getTargetValue());
        vo.setObservedValue(record.getObservedValue());
        vo.setBreachFlag(record.getBreachFlag());
        vo.setEvaluationWindow(record.getEvaluationWindow());
        vo.setEvaluatedAt(record.getEvaluatedAt());
        return vo;
    }
}
