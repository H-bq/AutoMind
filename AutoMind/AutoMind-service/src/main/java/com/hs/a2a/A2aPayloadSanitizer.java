package com.hs.a2a;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class A2aPayloadSanitizer {

    private static final Set<String> ALLOW_LIST = Set.of(
            "linkId",
            "latencyMs",
            "packetLossRate",
            "availableBandwidthMbps",
            "updatedAt",
            "links"
    );

    private static final Set<String> DENY_LIST = Set.of(
            "inputText",
            "parsedIntent",
            "managementIp",
            "actorId"
    );

    public Map<String, Object> sanitize(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> sanitized = new LinkedHashMap<>();
        payload.forEach((key, value) -> {
            if (key == null) {
                return;
            }
            if (DENY_LIST.contains(key) || key.startsWith("personal_")) {
                return;
            }
            if ("links".equals(key) && value instanceof Iterable<?> items) {
                sanitized.put(key, sanitizeLinks(items));
                return;
            }
            if (ALLOW_LIST.contains(key)) {
                sanitized.put(key, value);
            }
        });
        return sanitized;
    }

    private java.util.List<Map<String, Object>> sanitizeLinks(Iterable<?> items) {
        java.util.List<Map<String, Object>> links = new java.util.ArrayList<>();
        for (Object item : items) {
            if (item instanceof Map<?, ?> map) {
                Map<String, Object> normalized = new LinkedHashMap<>();
                map.forEach((key, value) -> normalized.put(String.valueOf(key), value));
                links.add(sanitize(normalized));
            }
        }
        return links;
    }
}
