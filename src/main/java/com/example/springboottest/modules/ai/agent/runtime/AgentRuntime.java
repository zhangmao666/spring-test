package com.example.springboottest.modules.ai.agent.runtime;

import com.example.springboottest.modules.ai.agent.message.AgentMessage;

import java.util.List;
import java.util.function.Consumer;

public interface AgentRuntime {

    AgentResponse call(AgentCallContext context, AgentMessage userMessage, Consumer<com.example.springboottest.modules.ai.agent.event.AgentEvent> eventConsumer);

    default AgentResponse stream(AgentCallContext context, AgentMessage userMessage, Consumer<com.example.springboottest.modules.ai.agent.event.AgentEvent> eventConsumer) {
        return call(context, userMessage, eventConsumer);
    }

    default AgentResponse resume(String runId, List<AgentMessage> messages, Consumer<com.example.springboottest.modules.ai.agent.event.AgentEvent> eventConsumer) {
        return AgentResponse.builder()
                .success(false)
                .error("Resume is not implemented yet for runId: " + runId)
                .build();
    }
}
