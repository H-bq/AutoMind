package com.hs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hs.common.exception.CommonException;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.NetworkDomain;
import com.hs.domain.po.NetworkLink;
import com.hs.domain.po.NetworkLinkResource;
import com.hs.domain.po.NetworkNode;
import com.hs.domain.po.SlaRecord;
import com.hs.domain.po.TelemetryData;
import com.hs.domain.vo.LinkStatusVO;
import com.hs.domain.vo.OverviewDashboardVO;
import com.hs.domain.vo.ReadModelVO;
import com.hs.domain.vo.SlaVerdictVO;
import com.hs.domain.vo.TopologyVO;
import com.hs.service.INetworkDomainService;
import com.hs.service.INetworkLinkResourceService;
import com.hs.service.INetworkLinkService;
import com.hs.service.INetworkNodeService;
import com.hs.service.ISlaRecordService;
import com.hs.service.ITelemetryDataService;
import com.hs.service.ITopologyAggregateService;
import com.hs.sla.SlaDashboardAggregator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopologyAggregateServiceImpl implements ITopologyAggregateService {

    private final INetworkDomainService networkDomainService;
    private final INetworkNodeService networkNodeService;
    private final INetworkLinkService networkLinkService;
    private final INetworkLinkResourceService networkLinkResourceService;
    private final ISlaRecordService slaRecordService;
    private final ITelemetryDataService telemetryDataService;
    private final SlaDashboardAggregator slaDashboardAggregator;

    @Override
    public OverviewDashboardVO overview() {
        TopologyVO topology = globalTopology();
        Map<String, TopologyVO.LinkResourceVO> resourceByLinkId = topology.getLinkResources().stream()
                .collect(Collectors.toMap(TopologyVO.LinkResourceVO::getLinkId, Function.identity(), (left, right) -> left));
        Map<String, SlaRecord> latestSlaByLinkId = latestSlaByLinkId(topology.getLinks().stream()
                .map(TopologyVO.LinkVO::getId)
                .collect(Collectors.toSet()));
        Map<String, TelemetryData> latestTelemetryByLinkId = latestTelemetryByLinkId(topology.getLinks().stream()
                .map(TopologyVO.LinkVO::getId)
                .collect(Collectors.toSet()));

        OverviewDashboardVO vo = new OverviewDashboardVO();
        vo.setTopology(topology);
        vo.setSla(slaDashboardAggregator.aggregateConfiguredWindow());
        vo.setDomainCount(topology.getDomains().size());
        vo.setNodeCount(topology.getNodes().size());
        vo.setLinkCount(topology.getLinks().size());
        vo.setActiveLinkCount(topology.getLinks().stream().filter(link -> isActiveStatus(link.getStatus())).count());

        BigDecimal totalBandwidth = topology.getLinks().stream()
                .map(TopologyVO.LinkVO::getTotalBandwidthMbps)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal reservedBandwidth = topology.getLinks().stream()
                .map(TopologyVO.LinkVO::getReservedBandwidthMbps)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalBandwidthMbps(totalBandwidth);
        vo.setReservedBandwidthTotalMbps(reservedBandwidth);
        vo.setBandwidthReservationRate(totalBandwidth.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : reservedBandwidth.divide(totalBandwidth, 4, RoundingMode.HALF_UP));

        List<OverviewDashboardVO.LinkHealthVO> anomalyLinks = topology.getLinks().stream()
                .map(link -> toLinkHealthVO(link, resourceByLinkId.get(link.getId()), latestTelemetryByLinkId.get(link.getId()), latestSlaByLinkId.get(link.getId())))
                .filter(health -> Boolean.TRUE.equals(health.getAnomaly()))
                .toList();
        vo.setAnomalyLinkCount(anomalyLinks.size());
        vo.setAnomalyLinks(anomalyLinks);
        return vo;
    }

    @Override
    public TopologyVO globalTopology() {
        String tenantId = UserContext.requireTenantId();
        List<NetworkDomain> domains = networkDomainService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkDomain::getTenantId, tenantId)
                .list();
        List<NetworkNode> nodes = networkNodeService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkNode::getTenantId, tenantId)
                .list();
        List<NetworkLink> links = networkLinkService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkLink::getTenantId, tenantId)
                .list();
        List<NetworkLinkResource> resources = networkLinkResourceService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkLinkResource::getTenantId, tenantId)
                .list();
        return buildTopology(domains, nodes, links, resources);
    }

    @Override
    public TopologyVO domainTopology(String domainId) {
        String tenantId = UserContext.requireTenantId();

        List<NetworkNode> nodes = networkNodeService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkNode::getTenantId, tenantId)
                .eq(NetworkNode::getDomainId, domainId)
                .list();
        List<String> nodeIds = nodes.stream().map(NetworkNode::getId).toList();

        List<NetworkDomain> domains = networkDomainService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkDomain::getTenantId, tenantId)
                .eq(NetworkDomain::getId, domainId)
                .list();
        if (nodeIds.isEmpty()) {
            return buildTopology(domains, nodes, List.of(), List.of());
        }

        List<NetworkLink> links = networkLinkService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkLink::getTenantId, tenantId)
                .and(wrapper -> wrapper.in(NetworkLink::getSourceNodeId, nodeIds)
                        .or()
                        .in(NetworkLink::getTargetNodeId, nodeIds))
                .list();
        List<String> linkIds = links.stream().map(NetworkLink::getId).toList();
        List<NetworkLinkResource> resources = linkIds.isEmpty()
                ? List.of()
                : networkLinkResourceService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkLinkResource::getTenantId, tenantId)
                .in(NetworkLinkResource::getLinkId, linkIds)
                .list();
        return buildTopology(domains, nodes, links, resources);
    }

    @Override
    public LinkStatusVO linkStatus(String linkId) {
        String tenantId = UserContext.requireTenantId();
        NetworkLink link = networkLinkService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkLink::getTenantId, tenantId)
                .eq(NetworkLink::getId, linkId)
                .one();
        if (link == null) {
            throw new CommonException("链路不存在", 404);
        }
        NetworkLinkResource latestResource = networkLinkResourceService.getOne(
                new LambdaQueryWrapper<NetworkLinkResource>()
                        .eq(!UserContext.isAdminTenant(tenantId), NetworkLinkResource::getTenantId, tenantId)
                        .eq(NetworkLinkResource::getLinkId, linkId)
                        .orderByDesc(NetworkLinkResource::getSampledAt)
                        .last("limit 1")
        );
        List<TelemetryData> telemetryTrend = telemetryDataService.list(
                new LambdaQueryWrapper<TelemetryData>()
                        .eq(!UserContext.isAdminTenant(tenantId), TelemetryData::getTenantId, tenantId)
                        .eq(TelemetryData::getLinkId, linkId)
                        .ge(TelemetryData::getTime, LocalDateTime.now().minusHours(24))
                        .orderByAsc(TelemetryData::getTime)
        );
        SlaRecord latestSla = slaRecordService.getOne(
                new LambdaQueryWrapper<SlaRecord>()
                        .eq(!UserContext.isAdminTenant(tenantId), SlaRecord::getTenantId, tenantId)
                        .eq(SlaRecord::getLinkId, linkId)
                        .orderByDesc(SlaRecord::getEvaluatedAt)
                        .last("limit 1")
        );
        NetworkNode sourceNode = findNode(link.getSourceNodeId(), tenantId);
        NetworkNode targetNode = findNode(link.getTargetNodeId(), tenantId);
        NetworkDomain sourceDomain = sourceNode == null ? null : findDomain(sourceNode.getDomainId(), tenantId);
        NetworkDomain targetDomain = targetNode == null ? null : findDomain(targetNode.getDomainId(), tenantId);

        LinkStatusVO vo = new LinkStatusVO();
        vo.setLink(toLinkVO(link));
        vo.setSourceNode(sourceNode == null ? null : toNodeVO(sourceNode));
        vo.setTargetNode(targetNode == null ? null : toNodeVO(targetNode));
        vo.setSourceDomain(sourceDomain == null ? null : toDomainVO(sourceDomain));
        vo.setTargetDomain(targetDomain == null ? null : toDomainVO(targetDomain));
        vo.setLatestResource(latestResource == null ? null : toLinkResourceVO(latestResource));
        vo.setLatestSlaVerdict(latestSla == null ? null : toSlaVerdictVO(latestSla));
        vo.setTelemetryTrend(telemetryTrend.stream().map(ReadModelVO::toTelemetryDataVO).toList());
        if (!telemetryTrend.isEmpty()) {
            vo.setLatestTelemetry(ReadModelVO.toTelemetryDataVO(telemetryTrend.get(telemetryTrend.size() - 1)));
        }
        validateLinkDetail(link, sourceNode, targetNode, sourceDomain, targetDomain, latestResource, vo.getDataIssues());
        return vo;
    }

    private TopologyVO buildTopology(List<NetworkDomain> domains,
                                     List<NetworkNode> nodes,
                                     List<NetworkLink> links,
                                     List<NetworkLinkResource> resources) {
        TopologyVO vo = new TopologyVO();
        vo.setDomains(domains.stream().map(this::toDomainVO).toList());
        vo.setNodes(nodes.stream().map(this::toNodeVO).toList());
        vo.setLinks(links.stream().map(this::toLinkVO).toList());
        vo.setLinkResources(latestResourceByLinkId(resources).values().stream()
                .sorted(Comparator.comparing(NetworkLinkResource::getLinkId, Comparator.nullsLast(String::compareTo)))
                .map(this::toLinkResourceVO)
                .toList());
        validateTopology(domains, nodes, links, resources, vo.getDataIssues());
        return vo;
    }

    private Map<String, NetworkLinkResource> latestResourceByLinkId(List<NetworkLinkResource> resources) {
        return resources.stream()
                .filter(resource -> resource.getLinkId() != null)
                .collect(Collectors.toMap(
                        NetworkLinkResource::getLinkId,
                        Function.identity(),
                        (left, right) -> latestResource(left, right),
                        HashMap::new
                ));
    }

    private NetworkLinkResource latestResource(NetworkLinkResource left, NetworkLinkResource right) {
        if (left.getSampledAt() == null) {
            return right;
        }
        if (right.getSampledAt() == null) {
            return left;
        }
        return left.getSampledAt().isAfter(right.getSampledAt()) ? left : right;
    }

    private Map<String, SlaRecord> latestSlaByLinkId(Set<String> linkIds) {
        if (linkIds.isEmpty()) {
            return Map.of();
        }
        String tenantId = UserContext.requireTenantId();
        return slaRecordService.list(new LambdaQueryWrapper<SlaRecord>()
                        .eq(!UserContext.isAdminTenant(tenantId), SlaRecord::getTenantId, tenantId)
                        .in(SlaRecord::getLinkId, linkIds))
                .stream()
                .filter(record -> record.getLinkId() != null)
                .collect(Collectors.toMap(
                        SlaRecord::getLinkId,
                        Function.identity(),
                        (left, right) -> latestSla(left, right),
                        HashMap::new
                ));
    }

    private SlaRecord latestSla(SlaRecord left, SlaRecord right) {
        if (left.getEvaluatedAt() == null) {
            return right;
        }
        if (right.getEvaluatedAt() == null) {
            return left;
        }
        return left.getEvaluatedAt().isAfter(right.getEvaluatedAt()) ? left : right;
    }

    private Map<String, TelemetryData> latestTelemetryByLinkId(Set<String> linkIds) {
        if (linkIds.isEmpty()) {
            return Map.of();
        }
        String tenantId = UserContext.requireTenantId();
        return telemetryDataService.list(new LambdaQueryWrapper<TelemetryData>()
                        .eq(!UserContext.isAdminTenant(tenantId), TelemetryData::getTenantId, tenantId)
                        .in(TelemetryData::getLinkId, linkIds))
                .stream()
                .filter(record -> record.getLinkId() != null)
                .collect(Collectors.toMap(
                        TelemetryData::getLinkId,
                        Function.identity(),
                        (left, right) -> latestTelemetry(left, right),
                        HashMap::new
                ));
    }

    private TelemetryData latestTelemetry(TelemetryData left, TelemetryData right) {
        if (left.getTime() == null) {
            return right;
        }
        if (right.getTime() == null) {
            return left;
        }
        return left.getTime().isAfter(right.getTime()) ? left : right;
    }

    private OverviewDashboardVO.LinkHealthVO toLinkHealthVO(TopologyVO.LinkVO link,
                                                            TopologyVO.LinkResourceVO resource,
                                                            TelemetryData telemetry,
                                                            SlaRecord slaRecord) {
        OverviewDashboardVO.LinkHealthVO vo = new OverviewDashboardVO.LinkHealthVO();
        vo.setLinkId(link.getId());
        vo.setLinkCode(link.getLinkCode());
        vo.setLinkName(link.getLinkName());
        vo.setStatus(link.getStatus());
        if (telemetry != null) {
            vo.setLatencyMs(telemetry.getLatencyMs());
            vo.setPacketLossRate(telemetry.getPacketLossRate());
            vo.setBandwidthUsage(telemetry.getBandwidthUsage());
            vo.setJitterMs(telemetry.getJitterMs());
        } else if (resource != null) {
            vo.setLatencyMs(resource.getCurrentLatencyMs());
            vo.setPacketLossRate(resource.getCurrentPacketLossRate());
            vo.setJitterMs(resource.getCurrentJitterMs());
        }
        vo.setLatestSlaBreach(slaRecord == null ? null : slaRecord.getBreachFlag());
        vo.setAnomaly(Boolean.TRUE.equals(resource == null ? null : resource.getIsAnomaly())
                || Boolean.TRUE.equals(telemetry == null ? null : telemetry.getIsAnomaly())
                || Boolean.TRUE.equals(vo.getLatestSlaBreach())
                || isUnhealthyStatus(link.getStatus()));
        return vo;
    }

    private void validateTopology(List<NetworkDomain> domains,
                                  List<NetworkNode> nodes,
                                  List<NetworkLink> links,
                                  List<NetworkLinkResource> resources,
                                  List<TopologyVO.DataIssueVO> issues) {
        Set<String> domainIds = domains.stream().map(NetworkDomain::getId).collect(Collectors.toSet());
        Set<String> nodeIds = nodes.stream().map(NetworkNode::getId).collect(Collectors.toSet());
        Set<String> linkIds = links.stream().map(NetworkLink::getId).collect(Collectors.toSet());
        Set<String> resourceLinkIds = resources.stream()
                .map(NetworkLinkResource::getLinkId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        nodes.stream()
                .filter(node -> node.getDomainId() != null && !domainIds.contains(node.getDomainId()))
                .forEach(node -> addIssue(issues, "WARN", "MISSING_DOMAIN", "network_node", node.getId(),
                        "节点所属网络域不存在或不在当前租户范围内"));
        links.stream()
                .filter(link -> link.getSourceNodeId() == null || !nodeIds.contains(link.getSourceNodeId()))
                .forEach(link -> addIssue(issues, "ERROR", "MISSING_SOURCE_NODE", "network_link", link.getId(),
                        "链路源节点不存在或不在当前租户范围内"));
        links.stream()
                .filter(link -> link.getTargetNodeId() == null || !nodeIds.contains(link.getTargetNodeId()))
                .forEach(link -> addIssue(issues, "ERROR", "MISSING_TARGET_NODE", "network_link", link.getId(),
                        "链路目标节点不存在或不在当前租户范围内"));
        resourceLinkIds.stream()
                .filter(linkId -> !linkIds.contains(linkId))
                .forEach(linkId -> addIssue(issues, "WARN", "ORPHAN_LINK_RESOURCE", "network_link_resource", linkId,
                        "链路资源记录找不到对应链路"));
        links.stream()
                .filter(link -> !resourceLinkIds.contains(link.getId()))
                .forEach(link -> addIssue(issues, "INFO", "MISSING_LINK_RESOURCE", "network_link", link.getId(),
                        "链路缺少资源状态采样"));
    }

    private void validateLinkDetail(NetworkLink link,
                                    NetworkNode sourceNode,
                                    NetworkNode targetNode,
                                    NetworkDomain sourceDomain,
                                    NetworkDomain targetDomain,
                                    NetworkLinkResource latestResource,
                                    List<TopologyVO.DataIssueVO> issues) {
        if (sourceNode == null) {
            addIssue(issues, "ERROR", "MISSING_SOURCE_NODE", "network_link", link.getId(), "链路源节点不存在或不可见");
        }
        if (targetNode == null) {
            addIssue(issues, "ERROR", "MISSING_TARGET_NODE", "network_link", link.getId(), "链路目标节点不存在或不可见");
        }
        if (sourceNode != null && sourceDomain == null) {
            addIssue(issues, "WARN", "MISSING_SOURCE_DOMAIN", "network_node", sourceNode.getId(), "源节点所属网络域不存在或不可见");
        }
        if (targetNode != null && targetDomain == null) {
            addIssue(issues, "WARN", "MISSING_TARGET_DOMAIN", "network_node", targetNode.getId(), "目标节点所属网络域不存在或不可见");
        }
        if (latestResource == null) {
            addIssue(issues, "INFO", "MISSING_LINK_RESOURCE", "network_link", link.getId(), "链路缺少资源状态采样");
        }
    }

    private void addIssue(List<TopologyVO.DataIssueVO> issues,
                          String severity,
                          String issueType,
                          String entityType,
                          String entityId,
                          String message) {
        TopologyVO.DataIssueVO issue = new TopologyVO.DataIssueVO();
        issue.setSeverity(severity);
        issue.setIssueType(issueType);
        issue.setEntityType(entityType);
        issue.setEntityId(entityId);
        issue.setMessage(message);
        issues.add(issue);
    }

    private NetworkNode findNode(String nodeId, String tenantId) {
        if (nodeId == null || nodeId.isBlank()) {
            return null;
        }
        return networkNodeService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkNode::getTenantId, tenantId)
                .eq(NetworkNode::getId, nodeId)
                .one();
    }

    private NetworkDomain findDomain(String domainId, String tenantId) {
        if (domainId == null || domainId.isBlank()) {
            return null;
        }
        return networkDomainService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkDomain::getTenantId, tenantId)
                .eq(NetworkDomain::getId, domainId)
                .one();
    }

    private boolean isActiveStatus(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        return "ACTIVE".equals(normalized) || "UP".equals(normalized) || "ONLINE".equals(normalized) || "NORMAL".equals(normalized);
    }

    private boolean isUnhealthyStatus(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        return normalized.contains("DOWN")
                || normalized.contains("FAIL")
                || normalized.contains("ERROR")
                || normalized.contains("DEGRADED")
                || normalized.contains("ANOMALY");
    }

    private TopologyVO.DomainVO toDomainVO(NetworkDomain domain) {
        TopologyVO.DomainVO vo = new TopologyVO.DomainVO();
        vo.setId(domain.getId());
        vo.setDomainCode(domain.getDomainCode());
        vo.setDomainName(domain.getDomainName());
        vo.setControllerType(domain.getControllerType());
        vo.setStatus(domain.getStatus());
        return vo;
    }

    private TopologyVO.NodeVO toNodeVO(NetworkNode node) {
        TopologyVO.NodeVO vo = new TopologyVO.NodeVO();
        vo.setId(node.getId());
        vo.setDomainId(node.getDomainId());
        vo.setNodeCode(node.getNodeCode());
        vo.setNodeName(node.getNodeName());
        vo.setNodeType(node.getNodeType());
        vo.setLocation(node.getLocation());
        vo.setManagementIp(canViewManagementIp() ? node.getManagementIp() : null);
        vo.setStatus(node.getStatus());
        return vo;
    }

    private TopologyVO.LinkVO toLinkVO(NetworkLink link) {
        TopologyVO.LinkVO vo = new TopologyVO.LinkVO();
        vo.setId(link.getId());
        vo.setLinkCode(link.getLinkCode());
        vo.setLinkName(link.getLinkName());
        vo.setSourceNodeId(link.getSourceNodeId());
        vo.setTargetNodeId(link.getTargetNodeId());
        vo.setLinkType(link.getLinkType());
        vo.setTotalBandwidthMbps(link.getTotalBandwidthMbps());
        vo.setReservedBandwidthMbps(link.getReservedBandwidthMbps());
        vo.setMaxLatencyMs(link.getMaxLatencyMs());
        vo.setStatus(link.getStatus());
        return vo;
    }

    private TopologyVO.LinkResourceVO toLinkResourceVO(NetworkLinkResource resource) {
        TopologyVO.LinkResourceVO vo = new TopologyVO.LinkResourceVO();
        vo.setId(resource.getId());
        vo.setLinkId(resource.getLinkId());
        vo.setQosProfile(resource.getQosProfile());
        vo.setPriorityQueue(resource.getPriorityQueue());
        vo.setAvailableBandwidthMbps(resource.getAvailableBandwidthMbps());
        vo.setCurrentLatencyMs(resource.getCurrentLatencyMs());
        vo.setCurrentPacketLossRate(resource.getCurrentPacketLossRate());
        vo.setCurrentJitterMs(resource.getCurrentJitterMs());
        vo.setIsAnomaly(resource.getIsAnomaly());
        vo.setSampledAt(resource.getSampledAt());
        return vo;
    }

    private SlaVerdictVO toSlaVerdictVO(SlaRecord record) {
        return new SlaVerdictVO()
                .setLinkId(record.getLinkId())
                .setMetricType(record.getMetricType())
                .setObservedValue(record.getObservedValue())
                .setTargetValue(record.getTargetValue())
                .setBreachFlag(record.getBreachFlag())
                .setEvaluationWindow(record.getEvaluationWindow())
                .setEvaluatedAt(record.getEvaluatedAt());
    }

    private boolean canViewManagementIp() {
        String role = UserContext.role();
        return "ADMIN".equalsIgnoreCase(role)
                || "OPS".equalsIgnoreCase(role)
                || "OPERATOR".equalsIgnoreCase(role);
    }
}
