package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "拓扑聚合视图")
public class TopologyVO {

    @Schema(description = "网络域列表")
    private List<DomainVO> domains = new ArrayList<>();

    @Schema(description = "节点列表")
    private List<NodeVO> nodes = new ArrayList<>();

    @Schema(description = "链路列表")
    private List<LinkVO> links = new ArrayList<>();

    @Schema(description = "链路资源状态列表")
    private List<LinkResourceVO> linkResources = new ArrayList<>();

    @Schema(description = "拓扑数据完整性问题")
    private List<DataIssueVO> dataIssues = new ArrayList<>();

    @Data
    @Schema(description = "网络域视图")
    public static class DomainVO {
        @Schema(description = "网络域ID")
        private String id;
        @Schema(description = "网络域编码")
        private String domainCode;
        @Schema(description = "网络域名称")
        private String domainName;
        @Schema(description = "控制器类型")
        private String controllerType;
        @Schema(description = "网络域状态")
        private String status;
    }

    @Data
    @Schema(description = "网络节点视图")
    public static class NodeVO {
        @Schema(description = "节点ID")
        private String id;
        @Schema(description = "所属网络域ID")
        private String domainId;
        @Schema(description = "节点编码")
        private String nodeCode;
        @Schema(description = "节点名称")
        private String nodeName;
        @Schema(description = "节点类型")
        private String nodeType;
        @Schema(description = "位置")
        private String location;
        @Schema(description = "管理IP，仅运维角色可见")
        private String managementIp;
        @Schema(description = "节点状态")
        private String status;
    }

    @Data
    @Schema(description = "网络链路视图")
    public static class LinkVO {
        @Schema(description = "链路ID")
        private String id;
        @Schema(description = "链路编码")
        private String linkCode;
        @Schema(description = "链路名称")
        private String linkName;
        @Schema(description = "源节点ID")
        private String sourceNodeId;
        @Schema(description = "目标节点ID")
        private String targetNodeId;
        @Schema(description = "链路类型")
        private String linkType;
        @Schema(description = "总带宽 Mbps")
        private BigDecimal totalBandwidthMbps;
        @Schema(description = "已预留带宽 Mbps")
        private BigDecimal reservedBandwidthMbps;
        @Schema(description = "目标最大时延 ms")
        private BigDecimal maxLatencyMs;
        @Schema(description = "链路状态")
        private String status;
        @Schema(description = "租户ID")
        private String tenantId;
    }

    @Data
    @Schema(description = "链路资源状态视图")
    public static class LinkResourceVO {
        @Schema(description = "资源记录ID")
        private String id;
        @Schema(description = "链路ID")
        private String linkId;
        @Schema(description = "QoS 策略档位")
        private String qosProfile;
        @Schema(description = "优先级队列")
        private String priorityQueue;
        @Schema(description = "可用带宽 Mbps")
        private BigDecimal availableBandwidthMbps;
        @Schema(description = "当前时延 ms")
        private BigDecimal currentLatencyMs;
        @Schema(description = "当前丢包率")
        private BigDecimal currentPacketLossRate;
        @Schema(description = "当前抖动 ms")
        private BigDecimal currentJitterMs;
        @Schema(description = "是否异常")
        private Boolean isAnomaly;
        @Schema(description = "采样时间")
        private LocalDateTime sampledAt;
    }

    @Data
    @Schema(description = "拓扑数据完整性问题")
    public static class DataIssueVO {
        @Schema(description = "问题级别", example = "WARN")
        private String severity;
        @Schema(description = "问题类型", example = "MISSING_SOURCE_NODE")
        private String issueType;
        @Schema(description = "实体类型", example = "network_link")
        private String entityType;
        @Schema(description = "实体ID")
        private String entityId;
        @Schema(description = "问题说明")
        private String message;
    }
}
