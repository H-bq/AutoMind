package com.hs.agent.dto;

import com.hs.agent.core.Confident;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Planner Agent 路径规划结果")
public class PlanDecision implements Confident {

    private List<LinkRef> primaryPath = new ArrayList<>();
    private List<LinkRef> backupPath = new ArrayList<>();
    private BigDecimal estimatedLatencyMs;
    private BigDecimal estimatedLossRate;
    private BigDecimal reservedBandwidthMbps;
    private List<String> conflicts = new ArrayList<>();
    private Boolean requiresApproval;
    private Boolean crossDomainDegraded;
    private String riskLevel;
    private BigDecimal confidence;

    @Data
    public static class LinkRef {
        private String linkId;
        private String linkCode;
        private String sourceNodeId;
        private String targetNodeId;
    }
}
