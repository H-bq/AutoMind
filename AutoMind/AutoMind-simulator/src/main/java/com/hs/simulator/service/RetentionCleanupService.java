package com.hs.simulator.service;

import com.hs.simulator.config.SimulatorProperties;
import com.hs.simulator.mapper.SlaRecordMapper;
import com.hs.simulator.mapper.TelemetryDataMapper;
import com.hs.simulator.vo.CleanupResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RetentionCleanupService {

    private final SimulatorProperties properties;
    private final TelemetryDataMapper telemetryDataMapper;
    private final SlaRecordMapper slaRecordMapper;

    public CleanupResultVO cleanupExpired() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(properties.getRetentionMinutes());
        int telemetryDeleted = cleanupTelemetry(cutoff);
        int slaDeleted = cleanupSlaRecords(cutoff);
        return new CleanupResultVO()
                .setCutoffTime(cutoff)
                .setDeletedTelemetryRows(telemetryDeleted)
                .setDeletedSlaRecordRows(slaDeleted);
    }

    private int cleanupTelemetry(LocalDateTime cutoff) {
        int total = 0;
        for (int i = 0; i < properties.getCleanupMaxBatchesPerRun(); i++) {
            int deleted = telemetryDataMapper.deleteExpired(cutoff, properties.getCleanupBatchSize());
            total += deleted;
            if (deleted < properties.getCleanupBatchSize()) {
                break;
            }
        }
        return total;
    }

    private int cleanupSlaRecords(LocalDateTime cutoff) {
        int total = 0;
        for (int i = 0; i < properties.getCleanupMaxBatchesPerRun(); i++) {
            int deleted = slaRecordMapper.deleteExpired(cutoff, properties.getCleanupBatchSize());
            total += deleted;
            if (deleted < properties.getCleanupBatchSize()) {
                break;
            }
        }
        return total;
    }
}
