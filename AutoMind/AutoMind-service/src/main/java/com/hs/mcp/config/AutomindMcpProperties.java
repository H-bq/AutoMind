package com.hs.mcp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "automind.mcp")
public class AutomindMcpProperties {

    private String serverBaseUrl = "http://localhost:9090";
    private int timeoutMs = 5000;
    private int maxRetries = 3;
    private long retryBackoffMs = 200;
    private List<String> highRiskTools = List.of("set_qos", "block_flow");
    private List<ToolProperties> tools = List.of(
            new ToolProperties("get_topo", "v1", false, "查询当前域拓扑", List.of("ADMIN", "OPS", "OPERATOR", "AUDITOR")),
            new ToolProperties("set_qos", "v1", true, "设置链路 QoS 策略", List.of("ADMIN", "OPS", "OPERATOR")),
            new ToolProperties("block_flow", "v1", true, "阻断指定流量五元组", List.of("ADMIN", "OPS", "OPERATOR"))
    );
    private Map<String, Map<String, Integer>> faultInjection = new HashMap<>();

    public record ToolProperties(
            String name,
            String version,
            boolean highRisk,
            String description,
            List<String> requiredRoles
    ) {
    }
}
