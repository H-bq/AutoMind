package com.hs.agent.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "automind.agent")
public class AgentProperties {

    private Double confidenceThreshold = 0.7;

    private Long timeoutMs = 30000L;

    private Integer maxRetries = 3;
}
