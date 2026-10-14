package com.hs.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hs.domain.dto.AgentTaskQueryDTO;
import com.hs.domain.po.AgentTask;
import com.hs.domain.vo.AgentTaskGroupVO;
import org.apache.ibatis.annotations.Param;

public interface AgentTaskMapper extends BaseMapper<AgentTask> {

    Page<AgentTaskGroupVO> selectIntentTaskGroupPage(Page<AgentTaskGroupVO> page,
                                                     @Param("query") AgentTaskQueryDTO query,
                                                     @Param("tenantId") String tenantId,
                                                     @Param("adminTenant") boolean adminTenant);
}
