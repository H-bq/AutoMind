package com.hs.agent.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hs.agent.core.AgentContext;
import com.hs.agent.core.BaseAgent;
import com.hs.agent.core.ResilientChatClient;
import com.hs.agent.dto.ParsedIntent;
import com.hs.mapper.AgentTaskMapper;
import com.hs.service.IIntentRequestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class SupervisorAgent extends BaseAgent<String, ParsedIntent> {

    private final ResilientChatClient chatClient;
    private final Resource systemPrompt;

    public SupervisorAgent(AgentTaskMapper agentTaskMapper,
                           IIntentRequestService intentRequestService,
                           ObjectMapper objectMapper,
                           ResilientChatClient chatClient,
                           @Value("${automind.agent.supervisor.system-prompt-path:classpath:/prompts/supervisor.st}") Resource systemPrompt) {
        super(agentTaskMapper, intentRequestService, objectMapper);
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt;
    }

    @Override
    protected String agentRole() {
        return "SUPERVISOR";
    }

    @Override
    protected String taskType() {
        return "INTENT_PARSE";
    }

    @Override
    protected String initialPevState() {
        return "PLAN";
    }

    @Override
    protected ParsedIntent doExecute(AgentContext ctx, String input) {
        return chatClient.call(systemPrompt, input, ParsedIntent.class, ctx);
    }
}
