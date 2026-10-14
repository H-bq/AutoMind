package com.hs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hs.common.domain.PageDTO;
import com.hs.domain.dto.McpCallLogQueryDTO;
import com.hs.domain.po.McpCallLog;
import com.hs.domain.vo.McpCallLogVO;

import java.util.List;

public interface IMcpCallLogService extends IService<McpCallLog> {

    PageDTO<McpCallLogVO> pageLogs(McpCallLogQueryDTO query);

    McpCallLogVO detail(Long id);

    List<McpCallLogVO> byTrace(String traceId);
}
