package com.hs.a2a;

import com.hs.a2a.event.A2aDegradedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class A2aFallbackGuard {

    private final A2aMessageSender sender;
    private final ApplicationEventPublisher eventPublisher;

    public <T> T sendOrFallback(A2aMessage req, Duration timeout, Function<A2aMessage, T> responseMapper, Supplier<T> fallbackSupplier) {
        return sender.sendAndAwait(req, timeout)
                .map(responseMapper)
                .orElseGet(() -> {
                    A2aMessage degraded = req.withType(A2aMessageType.DEGRADED);
                    sender.saveMessage(degraded, "DEGRADED", null, LocalDateTime.now(), LocalDateTime.now());
                    eventPublisher.publishEvent(new A2aDegradedEvent(this, degraded));
                    return fallbackSupplier.get();
                });
    }
}
