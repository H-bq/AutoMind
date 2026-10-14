package com.hs.a2a.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "automind.a2a")
public class AutomindA2aProperties {

    private int timeoutMs = 500;
    private String sharedSecret = "dev-secret";
    private String inboxPath = "/a2a/inbox";
}
