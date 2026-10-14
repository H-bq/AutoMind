package com.hs.agent.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.agent.core.AgentContext;
import com.hs.agent.core.BaseAgent;
import com.hs.agent.core.ResilientChatClient;
import com.hs.agent.dto.ExecutionResult;
import com.hs.agent.dto.PlanDecision;
import com.hs.agent.tool.ToolRegistry;
import com.hs.mapper.AgentTaskMapper;
import com.hs.mcp.McpCallContext;
import com.hs.mcp.McpClient;
import com.hs.mcp.exception.McpException;
import com.hs.service.IIntentRequestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ExecutorAgent extends BaseAgent<PlanDecision, ExecutionResult> {

    private final ResilientChatClient chatClient;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;
    private final McpClient mcpClient;
    private final Resource systemPrompt;

    public ExecutorAgent(AgentTaskMapper agentTaskMapper,
                         IIntentRequestService intentRequestService,
                         ObjectMapper objectMapper,
                         ResilientChatClient chatClient,
                         ToolRegistry toolRegistry,
                         McpClient mcpClient,
                         @Value("${automind.agent.executor.system-prompt-path:classpath:/prompts/executor.st}") Resource systemPrompt) {
        super(agentTaskMapper, intentRequestService, objectMapper);
        this.chatClient = chatClient;
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
        this.mcpClient = mcpClient;
        this.systemPrompt = systemPrompt;
    }

    @Override
    protected String agentRole() {
        return "EXECUTOR";
    }

    @Override
    protected String taskType() {
        return "RESOURCE_RESERVATION";
    }

    @Override
    protected String initialPevState() {
        return "EXECUTE";
    }

    @Override
    protected ExecutionResult doExecute(AgentContext ctx, PlanDecision input) {
        try {
            ExecutionResult result = chatClient.call(systemPrompt, toJson(input), ExecutionResult.class, ctx, toolRegistry.toolsFor(agentRole()));
            invokeMcpSetQos(ctx, input);
            result.setExecutorVia("MCP");
            if (result.getStatus() == null || result.getStatus().isBlank()) {
                result.setStatus("SUCCESS");
            }
            if (result.getAffectedLinks() == null || result.getAffectedLinks().isEmpty()) {
                result.setAffectedLinks(input.getPrimaryPath().stream().map(PlanDecision.LinkRef::getLinkId).toList());
            }
            return result;
        } catch (McpException e) {
            throw e;
        }
    }

    private void invokeMcpSetQos(AgentContext ctx, PlanDecision input) {
        if (input == null || input.getPrimaryPath() == null || input.getPrimaryPath().isEmpty()) {
            return;
        }
        String linkId = input.getPrimaryPath().get(0).getLinkId();
        if (linkId == null || linkId.isBlank()) {
            return;
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("linkId", linkId);
        BigDecimal bandwidth = input.getReservedBandwidthMbps();
        if (bandwidth != null) {
            params.put("reservedBandwidthMbps", bandwidth);
        }
        params.put("priority", 1);
        mcpClient.invoke(McpCallContext.of(ctx, "set_qos", "v1"), params);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
