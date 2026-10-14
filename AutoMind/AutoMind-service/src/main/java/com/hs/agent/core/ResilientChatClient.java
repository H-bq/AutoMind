package com.hs.agent.core;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class ResilientChatClient {

    private final ChatClient.Builder chatClientBuilder;
    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final AgentProperties agentProperties;

    public <T extends Confident> T call(Resource systemPrompt,
                                        String userPrompt,
                                        Class<T> targetType,
                                        AgentContext context,
                                        Object... tools) {
        BigDecimal threshold = BigDecimal.valueOf(agentProperties.getConfidenceThreshold());
        StructuredOutputConverter<T> converter = StructuredOutputConverter.of(targetType, threshold);
        Supplier<T> supplier = () -> {
            AgentToolContextHolder.set(context);
            try {
                String timePrefix = "【当前时间: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) + "】\n";
                return chatClientBuilder.build()
                        .prompt()
                        .system(systemPrompt)
                        .user(timePrefix + userPrompt + "\n\n" + converter.getFormat())
                        .tools(tools == null ? new Object[0] : tools)
                        .toolContext(java.util.Map.of("agentContext", context))
                        .call()
                        .entity(converter);
            } finally {
                AgentToolContextHolder.clear();
            }
        };

        Retry retry = retryRegistry.retry("aiServiceRetry");
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("aiServiceCb");
        Supplier<T> decorated = CircuitBreaker.decorateSupplier(circuitBreaker, Retry.decorateSupplier(retry, supplier));
        return decorated.get();
    }
}
