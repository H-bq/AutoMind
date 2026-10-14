package com.hs.simulator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "automind.simulator")
public class SimulatorProperties {

    private boolean enabled = true;
    private String adminToken = "automind-simulator-admin-dev-token";
    private String serviceBaseUrl = "http://localhost:9090";
    private String internalToken = "automind-internal-dev-token";
    private boolean notifyServiceEnabled = true;
    private long generateIntervalMs = 5000;
    private long evaluateIntervalMs = 8000;
    private long cleanupIntervalMs = 30000;
    private int retentionMinutes = 1;
    private int cleanupBatchSize = 200;
    private int cleanupMaxBatchesPerRun = 10;
    private String evaluationWindow = "5m";
    private int anomalyBurstSamples = 30;
    private List<String> metrics = new ArrayList<>(List.of("LATENCY", "JITTER", "LOSS", "BANDWIDTH"));
    private DefaultTargets defaultTargets = new DefaultTargets();

     @Data
    public static class DefaultTargets {
        private BigDecimal latencyMs = BigDecimal.valueOf(50);
        private BigDecimal jitterMs = BigDecimal.valueOf(10);
        private BigDecimal lossRate = BigDecimal.valueOf(0.01);
        private BigDecimal bandwidthUsage = BigDecimal.valueOf(80);
    }
}
