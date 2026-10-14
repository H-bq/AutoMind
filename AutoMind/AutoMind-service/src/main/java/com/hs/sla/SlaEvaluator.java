package com.hs.sla;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hs.common.exception.BadRequestException;
import com.hs.common.utils.UserContext;
import com.hs.domain.dto.SlaEvaluateDTO;
import com.hs.domain.po.NetworkLink;
import com.hs.domain.po.NetworkLinkResource;
import com.hs.domain.po.SlaRecord;
import com.hs.domain.po.TelemetryData;
import com.hs.domain.vo.SlaVerdictVO;
import com.hs.service.INetworkLinkResourceService;
import com.hs.service.INetworkLinkService;
import com.hs.service.ISlaRecordService;
import com.hs.service.ITelemetryDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class SlaEvaluator {

    private final ITelemetryDataService telemetryDataService;
    private final INetworkLinkService networkLinkService;
    private final INetworkLinkResourceService networkLinkResourceService;
    private final ISlaRecordService slaRecordService;

    @Value("${automind.sla.default-window:5m}")
    private String defaultWindow;

    @Value("${automind.sla.default-targets.latency-ms:50}")
    private BigDecimal defaultLatencyMs;

    @Value("${automind.sla.default-targets.jitter-ms:10}")
    private BigDecimal defaultJitterMs;

    @Value("${automind.sla.default-targets.loss-rate:0.01}")
    private BigDecimal defaultLossRate;

    @Value("${automind.sla.default-targets.bandwidth-usage:0.8}")
    private BigDecimal defaultBandwidthUsage;

    public SlaVerdictVO evaluate(SlaEvaluateDTO dto) {
        if (dto == null) {
            throw new BadRequestException("SLA 评估请求不能为空");
        }
        return evaluate(dto.getLinkId(), dto.getMetricType(), dto.getWindow());
    }

    public SlaVerdictVO evaluate(String linkId, String metricType, String window) {
        String tenantId = UserContext.requireTenantId();
        if (linkId == null || linkId.isBlank()) {
            throw new BadRequestException("链路ID不能为空");
        }
        String normalizedMetric = normalizeMetric(metricType);
        String evaluationWindow = (window == null || window.isBlank()) ? defaultWindow : window;
        Duration duration = parseWindow(evaluationWindow);

        NetworkLink link = networkLinkService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), NetworkLink::getTenantId, tenantId)
                .eq(NetworkLink::getId, linkId)
                .one();
        if (link == null) {
            throw new BadRequestException("链路不存在或无权访问");
        }

        LocalDateTime from = LocalDateTime.now().minus(duration);
        List<TelemetryData> samples = telemetryDataService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), TelemetryData::getTenantId, tenantId)
                .eq(TelemetryData::getLinkId, linkId)
                .ge(TelemetryData::getTime, from)
                .list();

        BigDecimal observedValue = observedFromTelemetry(samples, normalizedMetric);
        if (observedValue == null) {
            observedValue = observedFromLatestResource(tenantId, linkId, normalizedMetric);
        }
        if (observedValue == null) {
            throw new BadRequestException("评估窗口内没有可用遥测或资源采样");
        }

        BigDecimal targetValue = targetValue(link, normalizedMetric);
        boolean breachFlag = observedValue.compareTo(targetValue) > 0;
        LocalDateTime evaluatedAt = LocalDateTime.now();

        SlaRecord record = new SlaRecord()
                .setTenantId(tenantId)
                .setIntentId(UserContext.intentId())
                .setLinkId(linkId)
                .setMetricType(normalizedMetric)
                .setTargetValue(targetValue)
                .setObservedValue(observedValue)
                .setBreachFlag(breachFlag)
                .setEvaluationWindow(evaluationWindow)
                .setEvaluatedAt(evaluatedAt);
        slaRecordService.save(record);

        return new SlaVerdictVO()
                .setLinkId(linkId)
                .setMetricType(normalizedMetric)
                .setObservedValue(observedValue)
                .setTargetValue(targetValue)
                .setBreachFlag(breachFlag)
                .setEvaluationWindow(evaluationWindow)
                .setEvaluatedAt(evaluatedAt);
    }

    private String normalizeMetric(String metricType) {
        if (metricType == null || metricType.isBlank()) {
            throw new BadRequestException("指标类型不能为空");
        }
        String metric = metricType.toUpperCase(Locale.ROOT);
        if (!List.of("LATENCY", "JITTER", "LOSS", "BANDWIDTH").contains(metric)) {
            throw new BadRequestException("不支持的指标类型：" + metricType);
        }
        return metric;
    }

    private BigDecimal observedFromTelemetry(List<TelemetryData> samples, String metricType) {
        Function<TelemetryData, BigDecimal> extractor = switch (metricType) {
            case "LATENCY" -> TelemetryData::getLatencyMs;
            case "JITTER" -> TelemetryData::getJitterMs;
            case "LOSS" -> TelemetryData::getPacketLossRate;
            case "BANDWIDTH" -> TelemetryData::getBandwidthUsage;
            default -> throw new BadRequestException("不支持的指标类型：" + metricType);
        };
        List<BigDecimal> values = samples.stream().map(extractor).filter(Objects::nonNull).toList();
        if (values.isEmpty()) {
            return null;
        }
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal observedFromLatestResource(String tenantId, String linkId, String metricType) {
        NetworkLinkResource resource = networkLinkResourceService.getOne(
                new LambdaQueryWrapper<NetworkLinkResource>()
                        .eq(!UserContext.isAdminTenant(tenantId), NetworkLinkResource::getTenantId, tenantId)
                        .eq(NetworkLinkResource::getLinkId, linkId)
                        .orderByDesc(NetworkLinkResource::getSampledAt)
                        .last("limit 1")
        );
        if (resource == null) {
            return null;
        }
        return switch (metricType) {
            case "LATENCY" -> resource.getCurrentLatencyMs();
            case "JITTER" -> resource.getCurrentJitterMs();
            case "LOSS" -> resource.getCurrentPacketLossRate();
            case "BANDWIDTH" -> null;
            default -> null;
        };
    }

    private BigDecimal targetValue(NetworkLink link, String metricType) {
        return switch (metricType) {
            case "LATENCY" -> link.getMaxLatencyMs() == null ? defaultLatencyMs : link.getMaxLatencyMs();
            case "JITTER" -> defaultJitterMs;
            case "LOSS" -> defaultLossRate;
            case "BANDWIDTH" -> defaultBandwidthUsage;
            default -> throw new BadRequestException("不支持的指标类型：" + metricType);
        };
    }

    private Duration parseWindow(String window) {
        String value = window.trim().toLowerCase(Locale.ROOT);
        try {
            if (value.endsWith("m")) {
                return Duration.ofMinutes(Long.parseLong(value.substring(0, value.length() - 1)));
            }
            if (value.endsWith("h")) {
                return Duration.ofHours(Long.parseLong(value.substring(0, value.length() - 1)));
            }
            if (value.endsWith("d")) {
                return Duration.ofDays(Long.parseLong(value.substring(0, value.length() - 1)));
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("评估窗口格式错误：" + window, e);
        }
        throw new BadRequestException("评估窗口仅支持 m/h/d 后缀：" + window);
    }
}
