package com.hs.agent.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.agent.core.AgentContext;
import com.hs.agent.core.BaseAgent;
import com.hs.agent.core.ResilientChatClient;
import com.hs.agent.dto.ExecutionResult;
import com.hs.agent.dto.SlaVerdict;
import com.hs.agent.tool.ToolRegistry;
import com.hs.mapper.AgentTaskMapper;
import com.hs.service.IIntentRequestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class MonitorAgent extends BaseAgent<ExecutionResult, SlaVerdict> {

    private final ResilientChatClient chatClient;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;
    private final Resource systemPrompt;

    public MonitorAgent(AgentTaskMapper agentTaskMapper,
                        IIntentRequestService intentRequestService,
                        ObjectMapper objectMapper,
                        ResilientChatClient chatClient,
                        ToolRegistry toolRegistry,
                        @Value("${automind.agent.monitor.system-prompt-path:classpath:/prompts/monitor.st}") Resource systemPrompt) {
        super(agentTaskMapper, intentRequestService, objectMapper);
        this.chatClient = chatClient;
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
        this.systemPrompt = systemPrompt;
    }

    @Override
    protected String agentRole() {
        return "MONITOR";
    }

    @Override
    protected String taskType() {
        return "SLA_VERIFY";
    }

    @Override
    protected String initialPevState() {
        return "VERIFY";
    }

    @Override
    protected SlaVerdict doExecute(AgentContext ctx, ExecutionResult input) {
        return chatClient.call(systemPrompt, toJson(input), SlaVerdict.class, ctx, toolRegistry.toolsFor(agentRole()));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
