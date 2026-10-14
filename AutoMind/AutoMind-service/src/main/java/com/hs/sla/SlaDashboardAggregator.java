package com.hs.sla;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.SlaRecord;
import com.hs.domain.vo.SlaDashboardVO;
import com.hs.service.ISlaRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SlaDashboardAggregator {

    private final ISlaRecordService slaRecordService;

    @Value("${automind.sla.dashboard-window:5m}")
    private String dashboardWindow;

    public SlaDashboardVO aggregateConfiguredWindow() {
        String tenantId = UserContext.requireTenantId();
        Duration duration = parseWindow(dashboardWindow);
        LocalDateTime from = LocalDateTime.now().minus(duration);
        List<SlaRecord> records = slaRecordService.list(
                new LambdaQueryWrapper<SlaRecord>()
                        .eq(!UserContext.isAdminTenant(tenantId), SlaRecord::getTenantId, tenantId)
                        .ge(SlaRecord::getEvaluatedAt, from)
                        .orderByAsc(SlaRecord::getEvaluatedAt)
        );

        SlaDashboardVO vo = new SlaDashboardVO();
        vo.setOverallComplianceRate(complianceRate(records));
        vo.setMetricCompliance(metricCompliance(records));
        vo.setBreachTrend(breachTrend(records, duration));
        return vo;
    }

    private List<SlaDashboardVO.MetricComplianceVO> metricCompliance(List<SlaRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(SlaRecord::getMetricType))
                .entrySet()
                .stream()
                .map(entry -> {
                    List<SlaRecord> metricRecords = entry.getValue();
                    SlaDashboardVO.MetricComplianceVO vo = new SlaDashboardVO.MetricComplianceVO();
                    vo.setMetricType(entry.getKey());
                    vo.setTotalCount((long) metricRecords.size());
                    vo.setBreachCount(breachCount(metricRecords));
                    vo.setComplianceRate(complianceRate(metricRecords));
                    return vo;
                })
                .sorted(Comparator.comparing(SlaDashboardVO.MetricComplianceVO::getMetricType))
                .toList();
    }

    private List<SlaDashboardVO.BreachTrendPointVO> breachTrend(List<SlaRecord> records, Duration duration) {
        ChronoUnit bucketUnit = duration.compareTo(Duration.ofHours(2)) <= 0
                ? ChronoUnit.MINUTES
                : ChronoUnit.HOURS;
        Map<LocalDateTime, Long> bucketed = records.stream()
                .filter(record -> Boolean.TRUE.equals(record.getBreachFlag()))
                .collect(Collectors.groupingBy(
                        record -> record.getEvaluatedAt().truncatedTo(bucketUnit),
                        TreeMap::new,
                        Collectors.counting()
                ));
        return bucketed.entrySet().stream().map(entry -> {
            SlaDashboardVO.BreachTrendPointVO vo = new SlaDashboardVO.BreachTrendPointVO();
            vo.setBucketTime(entry.getKey());
            vo.setBreachCount(entry.getValue());
            return vo;
        }).toList();
    }

    private BigDecimal complianceRate(List<SlaRecord> records) {
        if (records == null || records.isEmpty()) {
            return BigDecimal.ZERO;
        }
        long okCount = records.stream().filter(record -> !Boolean.TRUE.equals(record.getBreachFlag())).count();
        return BigDecimal.valueOf(okCount)
                .divide(BigDecimal.valueOf(records.size()), 4, RoundingMode.HALF_UP);
    }

    private Long breachCount(List<SlaRecord> records) {
        return records.stream().filter(record -> Boolean.TRUE.equals(record.getBreachFlag())).count();
    }

    private Duration parseWindow(String window) {
        String value = window == null || window.isBlank() ? "5m" : window.trim().toLowerCase();
        if (value.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(value.substring(0, value.length() - 1)));
        }
        if (value.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(value.substring(0, value.length() - 1)));
        }
        if (value.endsWith("d")) {
            return Duration.ofDays(Long.parseLong(value.substring(0, value.length() - 1)));
        }
        throw new IllegalArgumentException("SLA dashboard window only supports m/h/d suffix: " + window);
    }
}
