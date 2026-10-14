package com.hs.domain.vo;

import com.hs.common.utils.UserContext;
import com.hs.domain.po.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class ReadModelVO {

    private ReadModelVO() {
    }

    public static AuditLogVO toAuditLogVO(AuditLog source) {
        if (source == null) {
            return null;
        }
        AuditLogVO vo = new AuditLogVO();
        vo.setId(source.getId());
        vo.setTenantId(source.getTenantId());
        vo.setActorId(source.getActorId());
        vo.setActorRole(source.getActorRole());
        vo.setAction(source.getAction());
        vo.setCreatedAt(source.getCreatedAt());
        return vo;
    }

    public static IntentRequestVO toIntentRequestVO(IntentRequest source) {
        if (source == null) {
            return null;
        }
        IntentRequestVO vo = new IntentRequestVO();
        vo.setId(source.getId());
        vo.setTenantId(source.getTenantId());
        vo.setUserId(source.getUserId());
        vo.setInputText(source.getInputText());
        vo.setParsedIntent(source.getParsedIntent());
        vo.setPriority(source.getPriority());
        vo.setStatus(source.getStatus());
        vo.setTraceId(source.getTraceId());
        vo.setCreateTime(source.getCreateTime());
        return vo;
    }

    public static UserVO toUserVO(User source) {
        if (source == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setUserId(source.getUserId());
        vo.setUsername(source.getUsername());
        vo.setStatus(source.getStatus());
        vo.setIdentity(source.getIdentity());
        return vo;
    }

    public static PersonVO toPersonVO(Person source) {
        if (source == null) {
            return null;
        }
        PersonVO vo = new PersonVO();
        vo.setUserId(source.getUserId());
        vo.setPosition(source.getPosition());
        vo.setTenantId(source.getTenantId());
        vo.setName(source.getName());
        vo.setSex(source.getSex());
        vo.setIntroduce(source.getIntroduce());
        vo.setLabel(source.getLabel());
        return vo;
    }

    public static TelemetryDataVO toTelemetryDataVO(TelemetryData source) {
        if (source == null) {
            return null;
        }
        TelemetryDataVO vo = new TelemetryDataVO();
        vo.setTime(source.getTime());
        vo.setLinkId(source.getLinkId());
        vo.setLinkCode(source.getLinkCode());
        vo.setLatencyMs(source.getLatencyMs());
        vo.setPacketLossRate(source.getPacketLossRate());
        vo.setBandwidthUsage(source.getBandwidthUsage());
        vo.setJitterMs(source.getJitterMs());
        vo.setIsAnomaly(source.getIsAnomaly());
        return vo;
    }

    public static NetworkResourceReservationVO toNetworkResourceReservationVO(NetworkResourceReservation source) {
        if (source == null) {
            return null;
        }
        NetworkResourceReservationVO vo = new NetworkResourceReservationVO();
        vo.setId(source.getId());
        vo.setIntentRequestId(source.getIntentRequestId());
        vo.setLinkId(source.getLinkId());
        vo.setReservedBandwidthMbps(source.getReservedBandwidthMbps());
        vo.setTargetLatencyMs(source.getTargetLatencyMs());
        vo.setTargetPacketLossRate(source.getTargetPacketLossRate());
        vo.setPriority(source.getPriority());
        vo.setReservationStatus(source.getReservationStatus());
        vo.setEffectiveStart(source.getEffectiveStart());
        vo.setEffectiveEnd(source.getEffectiveEnd());
        vo.setCreatedAt(source.getCreatedAt());
        vo.setUpdatedAt(source.getUpdatedAt());
        return vo;
    }

    public static TopologyVO.DomainVO toDomainVO(NetworkDomain source) {
        if (source == null) {
            return null;
        }
        TopologyVO.DomainVO vo = new TopologyVO.DomainVO();
        vo.setId(source.getId());
        vo.setDomainCode(source.getDomainCode());
        vo.setDomainName(source.getDomainName());
        vo.setControllerType(source.getControllerType());
        vo.setStatus(source.getStatus());
        return vo;
    }

    public static TopologyVO.NodeVO toNodeVO(NetworkNode source) {
        if (source == null) {
            return null;
        }
        TopologyVO.NodeVO vo = new TopologyVO.NodeVO();
        vo.setId(source.getId());
        vo.setDomainId(source.getDomainId());
        vo.setNodeCode(source.getNodeCode());
        vo.setNodeName(source.getNodeName());
        vo.setNodeType(source.getNodeType());
        vo.setLocation(source.getLocation());
        vo.setManagementIp(canViewManagementIp() ? source.getManagementIp() : null);
        vo.setStatus(source.getStatus());
        return vo;
    }

    public static TopologyVO.LinkVO toLinkVO(NetworkLink source) {
        if (source == null) {
            return null;
        }
        TopologyVO.LinkVO vo = new TopologyVO.LinkVO();
        vo.setId(source.getId());
        vo.setLinkCode(source.getLinkCode());
        vo.setLinkName(source.getLinkName());
        vo.setSourceNodeId(source.getSourceNodeId());
        vo.setTargetNodeId(source.getTargetNodeId());
        vo.setLinkType(source.getLinkType());
        vo.setTotalBandwidthMbps(source.getTotalBandwidthMbps());
        vo.setReservedBandwidthMbps(source.getReservedBandwidthMbps());
        vo.setMaxLatencyMs(source.getMaxLatencyMs());
        vo.setStatus(source.getStatus());
        vo.setTenantId(source.getTenantId());
        return vo;
    }

    public static TopologyVO.LinkResourceVO toLinkResourceVO(NetworkLinkResource source) {
        if (source == null) {
            return null;
        }
        TopologyVO.LinkResourceVO vo = new TopologyVO.LinkResourceVO();
        vo.setId(source.getId());
        vo.setLinkId(source.getLinkId());
        vo.setQosProfile(source.getQosProfile());
        vo.setPriorityQueue(source.getPriorityQueue());
        vo.setAvailableBandwidthMbps(source.getAvailableBandwidthMbps());
        vo.setCurrentLatencyMs(source.getCurrentLatencyMs());
        vo.setCurrentPacketLossRate(source.getCurrentPacketLossRate());
        vo.setCurrentJitterMs(source.getCurrentJitterMs());
        vo.setIsAnomaly(source.getIsAnomaly());
        vo.setSampledAt(source.getSampledAt());
        return vo;
    }

    private static boolean canViewManagementIp() {
        String role = UserContext.role();
        return "ADMIN".equalsIgnoreCase(role)
                || "OPS".equalsIgnoreCase(role)
                || "OPERATOR".equalsIgnoreCase(role);
    }

    @Data
    @Schema(description = "审计日志视图")
    public static class AuditLogVO {
        private Integer id;
        private String tenantId;
        private Integer actorId;
        private String actorRole;
        private String action;
        private LocalDateTime createdAt;
    }

    @Data
    @Schema(description = "意图请求视图")
    public static class IntentRequestVO {
        private Integer id;
        private String tenantId;
        private Integer userId;
        private String inputText;
        private String parsedIntent;
        private Integer priority;
        private String status;
        private String traceId;
        private LocalDateTime createTime;
    }

    @Data
    @Schema(description = "用户视图")
    public static class UserVO {
        private Integer id;
        private Integer userId;
        private String username;
        private String status;
        private String identity;
    }

    @Data
    @Schema(description = "人员档案视图")
    public static class PersonVO {
        private Integer userId;
        private String position;
        private String tenantId;
        private String name;
        private String sex;
        private String introduce;
        private String label;
    }

    @Data
    @Schema(description = "遥测数据视图")
    public static class TelemetryDataVO {
        private LocalDateTime time;
        private String linkId;
        private String linkCode;
        private BigDecimal latencyMs;
        private BigDecimal packetLossRate;
        private BigDecimal bandwidthUsage;
        private BigDecimal jitterMs;
        private Boolean isAnomaly;
    }

    @Data
    @Schema(description = "网络资源预留视图")
    public static class NetworkResourceReservationVO {
        private String id;
        private Integer intentRequestId;
        private String linkId;
        private BigDecimal reservedBandwidthMbps;
        private BigDecimal targetLatencyMs;
        private BigDecimal targetPacketLossRate;
        private Integer priority;
        private String reservationStatus;
        private LocalDateTime effectiveStart;
        private LocalDateTime effectiveEnd;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
