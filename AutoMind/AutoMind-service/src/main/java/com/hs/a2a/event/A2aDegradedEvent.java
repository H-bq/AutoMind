package com.hs.a2a.event;

import com.hs.a2a.A2aMessage;
import org.springframework.context.ApplicationEvent;

public class A2aDegradedEvent extends ApplicationEvent {

    private final A2aMessage message;

    public A2aDegradedEvent(Object source, A2aMessage message) {
        super(source);
        this.message = message;
    }

    public A2aMessage message() {
        return message;
    }
}
