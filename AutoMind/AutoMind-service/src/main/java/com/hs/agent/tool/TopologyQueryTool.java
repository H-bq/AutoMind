package com.hs.agent.tool;

import com.hs.common.utils.UserContext;
import com.hs.domain.vo.TopologyVO;
import com.hs.service.ITopologyAggregateService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopologyQueryTool {

    private final ITopologyAggregateService topologyAggregateService;

    @Tool(name = "queryTopology", description = "查询当前租户网络拓扑；domainId 为空时返回全局拓扑")
    public TopologyVO query(String domainId) {
        UserContext.requireTenantId();
        if (domainId == null || domainId.isBlank()) {
            return topologyAggregateService.globalTopology();
        }
        return topologyAggregateService.domainTopology(domainId);
    }
}
