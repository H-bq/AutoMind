package com.hs.simulator.service;

import com.hs.simulator.config.SimulatorProperties;
import com.hs.simulator.dto.SlaRealtimeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class SlaEventNotifier {

    private final SimulatorProperties properties;
    private final RestClient restClient;

    public SlaEventNotifier(SimulatorProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.restClient = builder.baseUrl(trimTrailingSlash(properties.getServiceBaseUrl())).build();
    }

    public void notify(SlaRealtimeEvent event) {
        if (!properties.isNotifyServiceEnabled()) {
            return;
        }
        try {
            restClient.post()
                    .uri("/internal/sla-events")
                    .header("X-Internal-Token", properties.getInternalToken())
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to notify AutoMind-service SLA realtime event, linkId={}, metric={}",
                    event.getLinkId(), event.getMetricType(), e);
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:9090";
        }
        String result = value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
