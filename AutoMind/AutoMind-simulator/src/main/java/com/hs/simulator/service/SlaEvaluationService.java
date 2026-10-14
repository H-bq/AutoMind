package com.hs.simulator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hs.simulator.config.SimulatorProperties;
import com.hs.simulator.domain.NetworkLink;
import com.hs.simulator.domain.SlaRecord;
import com.hs.simulator.domain.TelemetryData;
import com.hs.simulator.dto.SlaRealtimeEvent;
import com.hs.simulator.mapper.NetworkLinkMapper;
import com.hs.simulator.mapper.SlaRecordMapper;
import com.hs.simulator.mapper.TelemetryDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SlaEvaluationService {

    private final SimulatorProperties properties;
    private final NetworkLinkMapper networkLinkMapper;
    private final TelemetryDataMapper telemetryDataMapper;
    private final SlaRecordMapper slaRecordMapper;
    private final SlaEventNotifier slaEventNotifier;

    public int evaluateAll() {
        List<NetworkLink> links = networkLinkMapper.selectList(new LambdaQueryWrapper<NetworkLink>()
                .isNotNull(NetworkLink::getTenantId)
                .isNotNull(NetworkLink::getId));
        return evaluateLinks(links);
    }

    public int evaluateLinks(List<NetworkLink> links) {
        int count = 0;
        for (NetworkLink link : links) {
            for (String metric : properties.getMetrics()) {
                SlaRecord record = evaluate(link, normalizeMetric(metric));
                if (record != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public int evaluateInjectedSamples(NetworkLink link, List<TelemetryData> samples) {
        int count = 0;
        for (String metric : properties.getMetrics()) {
            SlaRecord record = evaluateSamples(link, normalizeMetric(metric), samples, "ANOMALY_INJECTION");
            if (record != null) {
                count++;
            }
        }
        return count;
    }

    private SlaRecord evaluate(NetworkLink link, String metricType) {
        Duration duration = parseWindow(properties.getEvaluationWindow());
        LocalDateTime from = LocalDateTime.now().minus(duration);
        List<TelemetryData> samples = telemetryDataMapper.selectList(new LambdaQueryWrapper<TelemetryData>()
                .eq(TelemetryData::getTenantId, link.getTenantId())
                .eq(TelemetryData::getLinkId, link.getId())
                .ge(TelemetryData::getTime, from));
        return evaluateSamples(link, metricType, samples, properties.getEvaluationWindow());
    }

    private SlaRecord evaluateSamples(NetworkLink link, String metricType, List<TelemetryData> samples, String evaluationWindow) {
        BigDecimal observedValue = observedFromTelemetry(samples, metricType);
        if (observedValue == null) {
            return null;
        }

        BigDecimal targetValue = targetValue(link, metricType);
        boolean breachFlag = observedValue.compareTo(targetValue) > 0;
        LocalDateTime evaluatedAt = LocalDateTime.now();
        SlaRecord record = new SlaRecord()
                .setTenantId(link.getTenantId())
                .setLinkId(link.getId())
                .setMetricType(metricType)
                .setTargetValue(targetValue)
                .setObservedValue(observedValue)
                .setBreachFlag(breachFlag)
                .setEvaluationWindow(evaluationWindow)
                .setEvaluatedAt(evaluatedAt);
        slaRecordMapper.insert(record);
        slaEventNotifier.notify(toEvent(link, record));
        return record;
    }

    private BigDecimal observedFromTelemetry(List<TelemetryData> samples, String metricType) {
        Function<TelemetryData, BigDecimal> extractor = switch (metricType) {
            case "LATENCY" -> TelemetryData::getLatencyMs;
            case "JITTER" -> TelemetryData::getJitterMs;
            case "LOSS" -> TelemetryData::getPacketLossRate;
            case "BANDWIDTH" -> TelemetryData::getBandwidthUsage;
            default -> throw new IllegalArgumentException("Unsupported metric type: " + metricType);
        };
        List<BigDecimal> values = samples.stream().map(extractor).filter(Objects::nonNull).toList();
        if (values.isEmpty()) {
            return null;
        }
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal targetValue(NetworkLink link, String metricType) {
        SimulatorProperties.DefaultTargets targets = properties.getDefaultTargets();
        return switch (metricType) {
            case "LATENCY" -> link.getMaxLatencyMs() == null ? targets.getLatencyMs() : link.getMaxLatencyMs();
            case "JITTER" -> targets.getJitterMs();
            case "LOSS" -> targets.getLossRate();
            case "BANDWIDTH" -> targets.getBandwidthUsage();
            default -> throw new IllegalArgumentException("Unsupported metric type: " + metricType);
        };
    }

    private SlaRealtimeEvent toEvent(NetworkLink link, SlaRecord record) {
        return new SlaRealtimeEvent()
                .setTenantId(record.getTenantId())
                .setLinkId(record.getLinkId())
                .setLinkCode(link.getLinkCode())
                .setMetricType(record.getMetricType())
                .setObservedValue(record.getObservedValue())
                .setTargetValue(record.getTargetValue())
                .setBreachFlag(record.getBreachFlag())
                .setEvaluationWindow(record.getEvaluationWindow())
                .setEvaluatedAt(record.getEvaluatedAt())
                .setSource("SIMULATOR");
    }

    private String normalizeMetric(String metricType) {
        String metric = metricType == null ? "" : metricType.trim().toUpperCase(Locale.ROOT);
        if (!List.of("LATENCY", "JITTER", "LOSS", "BANDWIDTH").contains(metric)) {
            throw new IllegalArgumentException("Unsupported metric type: " + metricType);
        }
        return metric;
    }

    private Duration parseWindow(String window) {
        String value = window == null || window.isBlank() ? "5m" : window.trim().toLowerCase(Locale.ROOT);
        if (value.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(value.substring(0, value.length() - 1)));
        }
        if (value.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(value.substring(0, value.length() - 1)));
        }
        if (value.endsWith("d")) {
            return Duration.ofDays(Long.parseLong(value.substring(0, value.length() - 1)));
        }
        throw new IllegalArgumentException("Evaluation window only supports m/h/d suffix: " + window);
    }
}
