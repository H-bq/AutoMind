package com.hs.simulator.service;

import com.hs.simulator.config.SimulatorProperties;
import com.hs.simulator.domain.NetworkLink;
import com.hs.simulator.domain.TelemetryData;
import com.hs.simulator.dto.AnomalyGenerateDTO;
import com.hs.simulator.mapper.NetworkLinkMapper;
import com.hs.simulator.mapper.TelemetryDataMapper;
import com.hs.simulator.vo.AnomalyInjectionResultVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnomalyInjectionServiceTest {

    @Test
    void generatesAnomalySamplesAndEvaluatesInjectedSamples() {
        SimulatorProperties properties = new SimulatorProperties();
        properties.setAnomalyBurstSamples(3);
        NetworkLinkMapper networkLinkMapper = mock(NetworkLinkMapper.class);
        TelemetryDataMapper telemetryDataMapper = mock(TelemetryDataMapper.class);
        TelemetrySampleFactory sampleFactory = mock(TelemetrySampleFactory.class);
        SlaEvaluationService slaEvaluationService = mock(SlaEvaluationService.class);
        NetworkLink link = new NetworkLink()
                .setId("link-1")
                .setTenantId("tenant-001")
                .setLinkCode("link-code-1");
        when(networkLinkMapper.selectList(any())).thenReturn(List.of(link));
        when(sampleFactory.createAnomaly(any(NetworkLink.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> new TelemetryData()
                        .setLinkId("link-1")
                        .setTenantId("tenant-001")
                        .setLatencyMs(BigDecimal.valueOf(200))
                        .setIsAnomaly(true));
        when(slaEvaluationService.evaluateInjectedSamples(any(NetworkLink.class), any())).thenReturn(4);
        AnomalyInjectionService service = new AnomalyInjectionService(
                properties, networkLinkMapper, telemetryDataMapper, sampleFactory, slaEvaluationService);

        AnomalyInjectionResultVO result = service.generate(new AnomalyGenerateDTO());

        assertEquals(1, result.getLinkCount());
        assertEquals(3, result.getSampleCountPerLink());
        assertEquals(3, result.getGeneratedTelemetryCount());
        assertEquals(4, result.getGeneratedSlaRecordCount());
        verify(telemetryDataMapper, times(3)).insert(any(TelemetryData.class));
        verify(slaEvaluationService).evaluateInjectedSamples(eq(link), any());
    }
}
