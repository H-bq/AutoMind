package com.hs.realtime;

import com.hs.realtime.dto.SlaRealtimeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SlaRealtimePublisher {

    private final Map<String, Set<Client>> tenantClients = new ConcurrentHashMap<>();
    private final Set<Client> adminClients = ConcurrentHashMap.newKeySet();
    private final long timeoutMs;

    public SlaRealtimePublisher(@Value("${automind.realtime.sse-timeout-ms:300000}") long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public SseEmitter subscribe(String tenantId, boolean admin) {
        SseEmitter emitter = new SseEmitter(timeoutMs);
        Client client = new Client(UUID.randomUUID().toString(), tenantId, admin, emitter);
        if (admin) {
            adminClients.add(client);
        } else {
            tenantClients.computeIfAbsent(tenantId, key -> ConcurrentHashMap.newKeySet()).add(client);
        }

        Runnable cleanup = () -> remove(client);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());

        send(client, "connected", Map.of(
                "tenantId", tenantId,
                "admin", admin,
                "connectedAt", LocalDateTime.now().toString()
        ));
        log.info("SLA realtime client subscribed, id={}, tenantId={}, admin={}", client.id(), tenantId, admin);
        return emitter;
    }

    public void publish(SlaRealtimeEvent event) {
        if (event == null) {
            return;
        }
        adminClients.forEach(client -> send(client, "sla-event", event));
        if (event.getTenantId() != null && !event.getTenantId().isBlank()) {
            tenantClients.getOrDefault(event.getTenantId(), Set.of())
                    .forEach(client -> send(client, "sla-event", event));
        }
    }

    private void send(Client client, String eventName, Object payload) {
        try {
            client.emitter().send(SseEmitter.event()
                    .name(eventName)
                    .id(UUID.randomUUID().toString())
                    .data(payload));
        } catch (IOException | IllegalStateException e) {
            remove(client);
            log.debug("SLA realtime client removed after send failure, id={}", client.id(), e);
        }
    }

    private void remove(Client client) {
        if (client.admin()) {
            adminClients.remove(client);
        } else {
            Set<Client> clients = tenantClients.get(client.tenantId());
            if (clients != null) {
                clients.remove(client);
                if (clients.isEmpty()) {
                    tenantClients.remove(client.tenantId());
                }
            }
        }
    }

    private record Client(String id, String tenantId, boolean admin, SseEmitter emitter) {
    }
}
