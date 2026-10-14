package com.hs.mcp.server.handler;

import com.hs.mcp.McpErrorType;
import com.hs.mcp.config.AutomindMcpProperties;
import com.hs.service.ITopologyAggregateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetTopoToolHandler {

    private final ITopologyAggregateService topologyAggregateService;
    private final McpFaultInjector faultInjector;

    public ResponseEntity<Map<String, Object>> handle(Map<String, Object> params) {
        ResponseEntity<Map<String, Object>> injected = faultInjector.maybeFail("get_topo");
        if (injected != null) {
            return injected;
        }
        return ResponseEntity.ok(Map.of("topology", topologyAggregateService.globalTopology()));
    }
}
