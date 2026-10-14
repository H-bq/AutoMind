package com.hs.simulator.service;

import com.hs.simulator.config.SimulatorProperties;
import com.hs.simulator.domain.NetworkLink;
import com.hs.simulator.domain.TelemetryData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TelemetrySampleFactoryTest {

    @Test
    void createsNormalTelemetryInsideExpectedRange() {
        SimulatorProperties properties = new SimulatorProperties();
        TelemetrySampleFactory factory = new TelemetrySampleFactory(properties);
        NetworkLink link = new NetworkLink()
                .setId("link-1")
                .setTenantId("tenant-001")
                .setLinkCode("link-code-1")
                .setMaxLatencyMs(BigDecimal.valueOf(50))
                .setStatus("UP");

        TelemetryData sample = factory.create(link, LocalDateTime.now());

        assertEquals("tenant-001", sample.getTenantId());
        assertEquals("link-1", sample.getLinkId());
        assertEquals("link-code-1", sample.getLinkCode());
        assertFalse(sample.getIsAnomaly());
        assertTrue(sample.getLatencyMs().compareTo(BigDecimal.valueOf(17.50)) >= 0);
        assertTrue(sample.getLatencyMs().compareTo(BigDecimal.valueOf(42.50)) <= 0);
        assertTrue(sample.getBandwidthUsage().compareTo(BigDecimal.valueOf(30.00)) >= 0);
        assertTrue(sample.getBandwidthUsage().compareTo(BigDecimal.valueOf(75.00)) <= 0);
        assertNotNull(sample.getPacketLossRate());
        assertNotNull(sample.getJitterMs());
    }

    @Test
    void createsAnomalyTelemetryAboveTargets() {
        SimulatorProperties properties = new SimulatorProperties();
        TelemetrySampleFactory factory = new TelemetrySampleFactory(properties);
        NetworkLink link = new NetworkLink()
                .setId("link-1")
                .setTenantId("tenant-001")
                .setLinkCode("link-code-1")
                .setMaxLatencyMs(BigDecimal.valueOf(50))
                .setStatus("UP");

        TelemetryData sample = factory.createAnomaly(link, LocalDateTime.now());

        assertTrue(sample.getIsAnomaly());
        assertTrue(sample.getLatencyMs().compareTo(BigDecimal.valueOf(50)) > 0);
        assertTrue(sample.getJitterMs().compareTo(properties.getDefaultTargets().getJitterMs()) > 0);
        assertTrue(sample.getPacketLossRate().compareTo(properties.getDefaultTargets().getLossRate()) > 0);
        assertTrue(sample.getBandwidthUsage().compareTo(properties.getDefaultTargets().getBandwidthUsage()) > 0);
    }
}
