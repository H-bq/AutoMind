package com.hs.agent.tool;

import com.hs.common.exception.BadRequestException;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.NetworkLink;
import com.hs.service.INetworkLinkService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SlaTargetQueryTool {

    private final INetworkLinkService networkLinkService;

    @Value("${automind.sla.default-targets.loss-rate:0.01}")
    private BigDecimal defaultLossRate;

    @Tool(name = "querySlaTarget", description = "查询当前租户链路 SLA 目标")
    public SlaTarget targetFor(String linkId) {
        String tenantId = UserContext.requireTenantId();
        NetworkLink link = networkLinkService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkLink::getTenantId, tenantId)
                .eq(NetworkLink::getId, linkId)
                .one();
        if (link == null) {
            throw new BadRequestException("链路不存在或无权访问");
        }
        SlaTarget target = new SlaTarget();
        target.setLinkId(link.getId());
        target.setMaxLatencyMs(link.getMaxLatencyMs());
        target.setMaxLossRate(defaultLossRate);
        target.setTotalBandwidthMbps(link.getTotalBandwidthMbps());
        target.setReservedBandwidthMbps(link.getReservedBandwidthMbps());
        return target;
    }

    @Data
    public static class SlaTarget {
        private String linkId;
        private BigDecimal maxLatencyMs;
        private BigDecimal maxLossRate;
        private BigDecimal totalBandwidthMbps;
        private BigDecimal reservedBandwidthMbps;
    }
}
