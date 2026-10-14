package com.hs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hs.common.domain.PageDTO;
import com.hs.domain.dto.HealingRecordQueryDTO;
import com.hs.domain.po.HealingRecord;
import com.hs.domain.vo.HealingRecordVO;

import java.util.List;

public interface IHealingRecordService extends IService<HealingRecord> {

    PageDTO<HealingRecordVO> pageRecords(HealingRecordQueryDTO query);

    HealingRecordVO detail(Long id);

    List<HealingRecordVO> byIntent(Integer intentId);
}
