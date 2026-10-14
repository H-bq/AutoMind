package com.hs.controller;

import com.hs.common.domain.R;
import com.hs.domain.vo.LinkStatusVO;
import com.hs.domain.vo.OverviewDashboardVO;
import com.hs.domain.vo.TopologyVO;
import com.hs.service.ITopologyAggregateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Topology", description = "B端全局拓扑聚合接口")
@RestController
@RequestMapping("/topology")
@RequiredArgsConstructor
public class TopologyController {

    private final ITopologyAggregateService topologyAggregateService;

    @Operation(summary = "查询总览首页", description = "聚合拓扑 KPI、最新链路健康状态、SLA 趋势和数据完整性问题")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = OverviewDashboardVO.class)))
    @GetMapping("/overview")
    public R<OverviewDashboardVO> overview() {
        return R.ok(topologyAggregateService.overview());
    }

    @Operation(summary = "查询全局拓扑", description = "聚合网络域、节点、链路和链路资源状态，并按租户上下文过滤")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = TopologyVO.class)))
    @GetMapping("/global")
    public R<TopologyVO> globalTopology() {
        return R.ok(topologyAggregateService.globalTopology());
    }

//    @Operation(summary = "查询单域拓扑", description = "查询指定网络域下的节点、链路和链路资源状态")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = TopologyVO.class)))
    @GetMapping("/domain/{domainId}")
    public R<TopologyVO> domainTopology(
            @Parameter(description = "网络域ID", required = true)
            @PathVariable("domainId") String domainId) {
        return R.ok(topologyAggregateService.domainTopology(domainId));
    }

    @Operation(summary = "查询链路实时状态", description = "拼接链路静态信息、最近资源采样和最近 SLA 评估结论")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = LinkStatusVO.class)))
    @GetMapping("/link/{linkId}/status")
    public R<LinkStatusVO> linkStatus(
            @Parameter(description = "链路ID", required = true)
            @PathVariable("linkId") String linkId) {
        return R.ok(topologyAggregateService.linkStatus(linkId));
    }
}
