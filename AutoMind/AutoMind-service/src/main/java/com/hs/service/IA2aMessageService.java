package com.hs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hs.common.domain.PageDTO;
import com.hs.domain.dto.A2aMessageQueryDTO;
import com.hs.domain.po.A2aMessagePO;
import com.hs.domain.vo.A2aMessageVO;

import java.util.List;

public interface IA2aMessageService extends IService<A2aMessagePO> {

    PageDTO<A2aMessageVO> pageMessages(A2aMessageQueryDTO query);

    A2aMessageVO detail(Long id);

    List<A2aMessageVO> byTrace(String traceId);
}
