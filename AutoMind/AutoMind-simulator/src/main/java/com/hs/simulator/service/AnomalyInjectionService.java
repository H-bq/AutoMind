package com.hs.simulator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hs.common.exception.BadRequestException;
import com.hs.simulator.config.SimulatorProperties;
import com.hs.simulator.domain.NetworkLink;
import com.hs.simulator.domain.TelemetryData;
import com.hs.simulator.dto.AnomalyGenerateDTO;
import com.hs.simulator.mapper.NetworkLinkMapper;
import com.hs.simulator.mapper.TelemetryDataMapper;
import com.hs.simulator.vo.AnomalyInjectionResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnomalyInjectionService {

    private static final int MAX_SAMPLE_COUNT = 120;

    private final SimulatorProperties properties;
    private final NetworkLinkMapper networkLinkMapper;
    private final TelemetryDataMapper telemetryDataMapper;
    private final TelemetrySampleFactory telemetrySampleFactory;
    private final SlaEvaluationService slaEvaluationService;

    public AnomalyInjectionResultVO generate(AnomalyGenerateDTO request) {
        int sampleCount = normalizeSampleCount(request == null ? null : request.getSampleCount());
        List<NetworkLink> allLinks = loadLinks(request == null ? null : request.getLinkId());
        if (allLinks.isEmpty()) {
            throw new BadRequestException("没有可注入异常数据的链路");
        }

        // 默认情况下（未指定链路 ID）仅对单条链路生成异常数据，以避免产生海量告警气泡
        List<NetworkLink> links;
        if (request == null || request.getLinkId() == null || request.getLinkId().isBlank()) {
            links = List.of(allLinks.get(0));
        } else {
            links = allLinks;
        }

        LocalDateTime injectedAt = LocalDateTime.now();
        int telemetryCount = 0;
        int slaRecordCount = 0;
        for (NetworkLink link : links) {
            List<TelemetryData> injectedSamples = new ArrayList<>(sampleCount);
            for (int i = 0; i < sampleCount; i++) {
                TelemetryData sample = telemetrySampleFactory.createAnomaly(link, injectedAt.plusNanos(i * 1_000_000L));
                telemetryDataMapper.insert(sample);
                injectedSamples.add(sample);
                telemetryCount++;
            }
            slaRecordCount += slaEvaluationService.evaluateInjectedSamples(link, injectedSamples);
        }

        return new AnomalyInjectionResultVO()
                .setInjectedAt(injectedAt)
                .setLinkCount(links.size())
                .setSampleCountPerLink(sampleCount)
                .setGeneratedTelemetryCount(telemetryCount)
                .setGeneratedSlaRecordCount(slaRecordCount);
    }

    private List<NetworkLink> loadLinks(String linkId) {
        LambdaQueryWrapper<NetworkLink> wrapper = new LambdaQueryWrapper<NetworkLink>()
                .isNotNull(NetworkLink::getTenantId)
                .isNotNull(NetworkLink::getId);
        if (linkId != null && !linkId.isBlank()) {
            wrapper.eq(NetworkLink::getId, linkId.trim());
        }
        return networkLinkMapper.selectList(wrapper);
    }

    private int normalizeSampleCount(Integer requested) {
        int sampleCount = requested == null ? properties.getAnomalyBurstSamples() : requested;
        if (sampleCount <= 0) {
            throw new BadRequestException("异常采样数量必须大于 0");
        }
        return Math.min(sampleCount, MAX_SAMPLE_COUNT);
    }
}
