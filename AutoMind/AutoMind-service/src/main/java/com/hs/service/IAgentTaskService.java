package com.hs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hs.common.domain.PageDTO;
import com.hs.domain.dto.AgentTaskQueryDTO;
import com.hs.domain.po.AgentTask;
import com.hs.domain.vo.AgentExecutionTimelineVO;
import com.hs.domain.vo.AgentTaskGroupVO;
import com.hs.domain.vo.AgentTaskVO;

import java.util.List;

public interface IAgentTaskService extends IService<AgentTask> {

    PageDTO<AgentTaskGroupVO> pageTasks(AgentTaskQueryDTO query);

    AgentTaskVO detail(Long id);

    List<AgentTaskVO> byTrace(String traceId);

    List<AgentTaskVO> byIntent(Integer intentId);

    AgentExecutionTimelineVO timelineByIntent(Integer intentId);

    AgentExecutionTimelineVO timelineByTrace(String traceId);
}
