package com.hs.simulator.service;

import com.hs.simulator.config.SimulatorProperties;
import com.hs.simulator.domain.NetworkLink;
import com.hs.simulator.domain.TelemetryData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class TelemetrySampleFactory {

    private final SimulatorProperties properties;

    public TelemetryData create(NetworkLink link, LocalDateTime sampledAt) {
        SimulatorProperties.DefaultTargets targets = properties.getDefaultTargets();
        BigDecimal latencyTarget = link.getMaxLatencyMs() == null ? targets.getLatencyMs() : link.getMaxLatencyMs();

        BigDecimal latency = scaled(latencyTarget, random(0.35, 0.85), 2);
        BigDecimal jitter = scaled(targets.getJitterMs(), random(0.20, 0.80), 2);
        BigDecimal loss = scaled(targets.getLossRate(), random(0.05, 0.80), 4);
        BigDecimal bandwidth = decimal(random(30.00, 75.00), 2);

        return base(link, sampledAt)
                .setLatencyMs(latency)
                .setJitterMs(jitter)
                .setPacketLossRate(loss)
                .setBandwidthUsage(bandwidth)
                .setIsAnomaly(false);
    }

    public TelemetryData createAnomaly(NetworkLink link, LocalDateTime sampledAt) {
        SimulatorProperties.DefaultTargets targets = properties.getDefaultTargets();
        BigDecimal latencyTarget = link.getMaxLatencyMs() == null ? targets.getLatencyMs() : link.getMaxLatencyMs();

        return base(link, sampledAt)
                .setLatencyMs(scaled(latencyTarget, random(3.00, 5.00), 2))
                .setJitterMs(scaled(targets.getJitterMs(), random(3.00, 5.00), 2))
                .setPacketLossRate(scaled(targets.getLossRate(), random(8.00, 15.00), 4))
                .setBandwidthUsage(scaled(targets.getBandwidthUsage(), random(1.25, 1.50), 2))
                .setIsAnomaly(true);
    }

    private TelemetryData base(NetworkLink link, LocalDateTime sampledAt) {
        return new TelemetryData()
                .setTime(sampledAt)
                .setTenantId(link.getTenantId())
                .setLinkId(link.getId())
                .setLinkCode(link.getLinkCode());
    }

    private BigDecimal scaled(BigDecimal base, double multiplier, int scale) {
        return base.multiply(BigDecimal.valueOf(multiplier)).setScale(scale, RoundingMode.HALF_UP);
    }

    private BigDecimal decimal(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    private double random(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
