package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "总览首页聚合视图")
public class OverviewDashboardVO {

    @Schema(description = "网络域数量")
    private long domainCount;

    @Schema(description = "节点数量")
    private long nodeCount;

    @Schema(description = "链路数量")
    private long linkCount;

    @Schema(description = "运行中链路数量")
    private long activeLinkCount;

    @Schema(description = "异常链路数量")
    private long anomalyLinkCount;

    @Schema(description = "资源预留带宽总量 Mbps")
    private BigDecimal reservedBandwidthTotalMbps = BigDecimal.ZERO;

    @Schema(description = "链路总带宽 Mbps")
    private BigDecimal totalBandwidthMbps = BigDecimal.ZERO;

    @Schema(description = "带宽预留率")
    private BigDecimal bandwidthReservationRate = BigDecimal.ZERO;

    @Schema(description = "拓扑聚合数据")
    private TopologyVO topology = new TopologyVO();

    @Schema(description = "当前配置窗口 SLA 聚合数据")
    private SlaDashboardVO sla = new SlaDashboardVO();

    @Schema(description = "异常链路摘要")
    private List<LinkHealthVO> anomalyLinks = new ArrayList<>();

    @Data
    @Schema(description = "链路健康摘要")
    public static class LinkHealthVO {
        @Schema(description = "链路ID")
        private String linkId;
        @Schema(description = "链路编码")
        private String linkCode;
        @Schema(description = "链路名称")
        private String linkName;
        @Schema(description = "链路状态")
        private String status;
        @Schema(description = "当前时延 ms")
        private BigDecimal latencyMs;
        @Schema(description = "当前丢包率")
        private BigDecimal packetLossRate;
        @Schema(description = "当前带宽使用率")
        private BigDecimal bandwidthUsage;
        @Schema(description = "当前抖动 ms")
        private BigDecimal jitterMs;
        @Schema(description = "最近 SLA 是否违约")
        private Boolean latestSlaBreach;
        @Schema(description = "是否异常")
        private Boolean anomaly;
    }
}
