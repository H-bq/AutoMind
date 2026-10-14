package com.hs.simulator.service;

import com.hs.simulator.config.SimulatorProperties;
import com.hs.simulator.vo.CleanupResultVO;
import com.hs.simulator.vo.SimulatorStatusVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SimulatorStateService {

    private final SimulatorProperties properties;
    private final AtomicBoolean running;
    private volatile LocalDateTime lastGeneratedAt;
    private volatile int lastGeneratedCount;
    private volatile LocalDateTime lastEvaluatedAt;
    private volatile int lastEvaluationCount;
    private volatile LocalDateTime lastCleanupAt;
    private volatile int lastTelemetryCleanupCount;
    private volatile int lastSlaRecordCleanupCount;

    public SimulatorStateService(SimulatorProperties properties) {
        this.properties = properties;
        this.running = new AtomicBoolean(properties.isEnabled());
    }

    public boolean isRunning() {
        return running.get();
    }

    public void start() {
        running.set(true);
    }

    public void stop() {
        running.set(false);
    }

    public void recordGenerated(int count) {
        lastGeneratedAt = LocalDateTime.now();
        lastGeneratedCount = count;
    }

    public void recordEvaluated(int count) {
        lastEvaluatedAt = LocalDateTime.now();
        lastEvaluationCount = count;
    }

    public void recordCleanup(CleanupResultVO result) {
        lastCleanupAt = LocalDateTime.now();
        lastTelemetryCleanupCount = result.getDeletedTelemetryRows();
        lastSlaRecordCleanupCount = result.getDeletedSlaRecordRows();
    }

    public SimulatorStatusVO status() {
        return new SimulatorStatusVO()
                .setRunning(isRunning())
                .setGenerateIntervalMs(properties.getGenerateIntervalMs())
                .setEvaluateIntervalMs(properties.getEvaluateIntervalMs())
                .setCleanupIntervalMs(properties.getCleanupIntervalMs())
                .setRetentionMinutes(properties.getRetentionMinutes())
                .setCleanupBatchSize(properties.getCleanupBatchSize())
                .setLastGeneratedAt(lastGeneratedAt)
                .setLastGeneratedCount(lastGeneratedCount)
                .setLastEvaluatedAt(lastEvaluatedAt)
                .setLastEvaluationCount(lastEvaluationCount)
                .setLastCleanupAt(lastCleanupAt)
                .setLastTelemetryCleanupCount(lastTelemetryCleanupCount)
                .setLastSlaRecordCleanupCount(lastSlaRecordCleanupCount);
    }
}
