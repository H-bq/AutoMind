package com.hs.mcp;

import com.hs.common.exception.CommonException;
import com.hs.mcp.config.AutomindMcpProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpToolRegistry {

    private final AutomindMcpProperties properties;
    private final Map<String, McpToolDef> tools = new ConcurrentHashMap<>();

    @PostConstruct
    void load() {
        tools.clear();
        for (AutomindMcpProperties.ToolProperties tool : properties.getTools()) {
            register(new McpToolDef(
                    tool.name(),
                    tool.version(),
                    tool.highRisk(),
                    "{}",
                    tool.description(),
                    normalizeRoles(tool.requiredRoles() == null ? Set.of() : Set.copyOf(tool.requiredRoles()))
            ));
        }
        log.info("MCP tools loaded: {}", tools.keySet());
    }

    public Optional<McpToolDef> find(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public McpToolDef require(String name) {
        return find(name).orElseThrow(() -> new CommonException("MCP 工具未注册：" + name, 404));
    }

    public boolean isHighRisk(String name) {
        return find(name).map(McpToolDef::highRisk).orElse(false);
    }

    private void register(McpToolDef def) {
        tools.put(def.name(), def);
    }

    private Set<String> normalizeRoles(Set<String> roles) {
        return roles.stream()
                .map(role -> role.toUpperCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public record McpToolDef(
            String name,
            String version,
            boolean highRisk,
            String paramSchema,
            String description,
            Set<String> requiredRoles
    ) {
    }
}
