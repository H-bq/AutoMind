package com.hs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hs.common.domain.PageDTO;
import com.hs.domain.dto.SlaRecordQueryDTO;
import com.hs.domain.po.SlaRecord;
import com.hs.domain.vo.SlaRecordVO;

public interface ISlaRecordService extends IService<SlaRecord> {

    PageDTO<SlaRecordVO> records(SlaRecordQueryDTO query);
}
