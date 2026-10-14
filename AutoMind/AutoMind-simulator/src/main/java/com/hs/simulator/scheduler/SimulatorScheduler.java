package com.hs.simulator.scheduler;

import com.hs.simulator.service.RetentionCleanupService;
import com.hs.simulator.service.SimulatorStateService;
import com.hs.simulator.service.SlaEvaluationService;
import com.hs.simulator.service.TelemetryGeneratorService;
import com.hs.simulator.vo.CleanupResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimulatorScheduler {

    private final SimulatorStateService simulatorStateService;
    private final TelemetryGeneratorService telemetryGeneratorService;
    private final SlaEvaluationService slaEvaluationService;
    private final RetentionCleanupService retentionCleanupService;

    @Scheduled(fixedDelayString = "${automind.simulator.generate-interval-ms:5000}")
    public void generateTelemetry() {
        if (!simulatorStateService.isRunning()) {
            return;
        }
        int count = telemetryGeneratorService.generateOnce();
        simulatorStateService.recordGenerated(count);
        log.debug("Generated telemetry samples, count={}", count);
    }

    @Scheduled(fixedDelayString = "${automind.simulator.evaluate-interval-ms:8000}")
    public void evaluateSla() {
        if (!simulatorStateService.isRunning()) {
            return;
        }
        int count = slaEvaluationService.evaluateAll();
        simulatorStateService.recordEvaluated(count);
        log.debug("Evaluated SLA records, count={}", count);
    }

    @Scheduled(fixedDelayString = "${automind.simulator.cleanup-interval-ms:30000}")
    public void cleanupExpiredData() {
        if (!simulatorStateService.isRunning()) {
            return;
        }
        CleanupResultVO result = retentionCleanupService.cleanupExpired();
        simulatorStateService.recordCleanup(result);
        if (result.getDeletedTelemetryRows() > 0 || result.getDeletedSlaRecordRows() > 0) {
            log.info("Cleaned simulator data, telemetryRows={}, slaRows={}, cutoff={}",
                    result.getDeletedTelemetryRows(), result.getDeletedSlaRecordRows(), result.getCutoffTime());
        }
    }
}
