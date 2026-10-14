package com.hs.simulator.service;

import com.hs.simulator.config.SimulatorProperties;
import com.hs.simulator.domain.NetworkLink;
import com.hs.simulator.domain.SlaRecord;
import com.hs.simulator.domain.TelemetryData;
import com.hs.simulator.dto.SlaRealtimeEvent;
import com.hs.simulator.mapper.NetworkLinkMapper;
import com.hs.simulator.mapper.SlaRecordMapper;
import com.hs.simulator.mapper.TelemetryDataMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlaEvaluationServiceTest {

    @Mock
    NetworkLinkMapper networkLinkMapper;
    @Mock
    TelemetryDataMapper telemetryDataMapper;
    @Mock
    SlaRecordMapper slaRecordMapper;
    @Mock
    SlaEventNotifier slaEventNotifier;

    @Test
    void evaluatesTelemetryWindowAndWritesSlaRecord() {
        SimulatorProperties properties = new SimulatorProperties();
        properties.setMetrics(List.of("LATENCY"));
        NetworkLink link = new NetworkLink()
                .setId("link-1")
                .setTenantId("tenant-001")
                .setLinkCode("link-code-1")
                .setMaxLatencyMs(BigDecimal.valueOf(50));
        when(networkLinkMapper.selectList(any())).thenReturn(List.of(link));
        when(telemetryDataMapper.selectList(any())).thenReturn(List.of(
                new TelemetryData().setLatencyMs(BigDecimal.valueOf(55)),
                new TelemetryData().setLatencyMs(BigDecimal.valueOf(65))
        ));
        when(slaRecordMapper.insert(any(SlaRecord.class))).thenReturn(1);
        SlaEvaluationService service = new SlaEvaluationService(
                properties, networkLinkMapper, telemetryDataMapper, slaRecordMapper, slaEventNotifier);

        int count = service.evaluateAll();

        assertEquals(1, count);
        ArgumentCaptor<SlaRecord> recordCaptor = ArgumentCaptor.forClass(SlaRecord.class);
        verify(slaRecordMapper).insert(recordCaptor.capture());
        SlaRecord record = recordCaptor.getValue();
        assertEquals("tenant-001", record.getTenantId());
        assertEquals("link-1", record.getLinkId());
        assertEquals("LATENCY", record.getMetricType());
        assertEquals(new BigDecimal("60.0000"), record.getObservedValue());
        assertEquals(BigDecimal.valueOf(50), record.getTargetValue());
        assertTrue(record.getBreachFlag());
        assertNotNull(record.getEvaluatedAt());

        ArgumentCaptor<SlaRealtimeEvent> eventCaptor = ArgumentCaptor.forClass(SlaRealtimeEvent.class);
        verify(slaEventNotifier).notify(eventCaptor.capture());
        assertEquals("SIMULATOR", eventCaptor.getValue().getSource());
        assertEquals("link-code-1", eventCaptor.getValue().getLinkCode());
    }
}
