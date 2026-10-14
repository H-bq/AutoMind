package com.hs.simulator.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class SimulatorStatusVO {

    private boolean running;
    private long generateIntervalMs;
    private long evaluateIntervalMs;
    private long cleanupIntervalMs;
    private int retentionMinutes;
    private int cleanupBatchSize;
    private LocalDateTime lastGeneratedAt;
    private int lastGeneratedCount;
    private LocalDateTime lastEvaluatedAt;
    private int lastEvaluationCount;
    private LocalDateTime lastCleanupAt;
    private int lastTelemetryCleanupCount;
    private int lastSlaRecordCleanupCount;
}
