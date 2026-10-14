package com.hs.agent.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.agent.core.AgentContext;
import com.hs.agent.core.BaseAgent;
import com.hs.agent.core.ResilientChatClient;
import com.hs.agent.dto.ParsedIntent;
import com.hs.agent.dto.PlanDecision;
import com.hs.agent.tool.ToolRegistry;
import com.hs.mapper.AgentTaskMapper;
import com.hs.service.IIntentRequestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class PlannerAgent extends BaseAgent<ParsedIntent, PlanDecision> {

    private final ResilientChatClient chatClient;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;
    private final Resource systemPrompt;

    public PlannerAgent(AgentTaskMapper agentTaskMapper,
                        IIntentRequestService intentRequestService,
                        ObjectMapper objectMapper,
                        ResilientChatClient chatClient,
                        ToolRegistry toolRegistry,
                        @Value("${automind.agent.planner.system-prompt-path:classpath:/prompts/planner.st}") Resource systemPrompt) {
        super(agentTaskMapper, intentRequestService, objectMapper);
        this.chatClient = chatClient;
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
        this.systemPrompt = systemPrompt;
    }

    @Override
    protected String agentRole() {
        return "PLANNER";
    }

    @Override
    protected String taskType() {
        return "PATH_PLAN";
    }

    @Override
    protected String initialPevState() {
        return "PLAN";
    }

    @Override
    protected PlanDecision doExecute(AgentContext ctx, ParsedIntent input) {
        return chatClient.call(systemPrompt, toJson(input), PlanDecision.class, ctx, toolRegistry.toolsFor(agentRole()));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
