package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "SLA 仪表盘视图")
public class SlaDashboardVO {

    @Schema(description = "当前配置窗口总达标率")
    private BigDecimal overallComplianceRate = BigDecimal.ZERO;

    @Schema(description = "按指标类型分组的达标率")
    private List<MetricComplianceVO> metricCompliance = new ArrayList<>();

    @Schema(description = "当前配置窗口违约趋势点")
    private List<BreachTrendPointVO> breachTrend = new ArrayList<>();

    @Data
    @Schema(description = "指标达标率")
    public static class MetricComplianceVO {
        @Schema(description = "指标类型", example = "LATENCY")
        private String metricType;

        @Schema(description = "样本总数", example = "100")
        private Long totalCount;

        @Schema(description = "违约数量", example = "5")
        private Long breachCount;

        @Schema(description = "达标率", example = "0.9500")
        private BigDecimal complianceRate;
    }

    @Data
    @Schema(description = "违约趋势点")
    public static class BreachTrendPointVO {
        @Schema(description = "时间桶")
        private LocalDateTime bucketTime;

        @Schema(description = "违约数量", example = "3")
        private Long breachCount;
    }
}
