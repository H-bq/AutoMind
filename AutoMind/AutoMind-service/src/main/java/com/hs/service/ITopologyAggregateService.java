package com.hs.service;

import com.hs.domain.vo.LinkStatusVO;
import com.hs.domain.vo.OverviewDashboardVO;
import com.hs.domain.vo.TopologyVO;

public interface ITopologyAggregateService {

    OverviewDashboardVO overview();

    TopologyVO globalTopology();

    TopologyVO domainTopology(String domainId);

    LinkStatusVO linkStatus(String linkId);
}
