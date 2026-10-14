package com.hs.simulator.service;

import com.hs.simulator.config.SimulatorProperties;
import com.hs.simulator.mapper.SlaRecordMapper;
import com.hs.simulator.mapper.TelemetryDataMapper;
import com.hs.simulator.vo.CleanupResultVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RetentionCleanupServiceTest {

    @Test
    void deletesExpiredRowsInBatches() {
        SimulatorProperties properties = new SimulatorProperties();
        properties.setCleanupBatchSize(200);
        properties.setCleanupMaxBatchesPerRun(3);
        TelemetryDataMapper telemetryDataMapper = mock(TelemetryDataMapper.class);
        SlaRecordMapper slaRecordMapper = mock(SlaRecordMapper.class);
        when(telemetryDataMapper.deleteExpired(any(LocalDateTime.class), eq(200))).thenReturn(200, 50);
        when(slaRecordMapper.deleteExpired(any(LocalDateTime.class), eq(200))).thenReturn(0);
        RetentionCleanupService service = new RetentionCleanupService(properties, telemetryDataMapper, slaRecordMapper);

        CleanupResultVO result = service.cleanupExpired();

        assertEquals(250, result.getDeletedTelemetryRows());
        assertEquals(0, result.getDeletedSlaRecordRows());
        assertNotNull(result.getCutoffTime());
        verify(telemetryDataMapper, times(2)).deleteExpired(any(LocalDateTime.class), eq(200));
        verify(slaRecordMapper, times(1)).deleteExpired(any(LocalDateTime.class), eq(200));
    }
}
