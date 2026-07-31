package com.example.springboottest.modules.ai.agent.runtime;

import com.example.springboottest.modules.ai.agent.event.AgentEvent;
import com.example.springboottest.modules.ai.agent.message.AgentMessage;
import lombok.Builder;

import java.util.List;

@Builder
public record AgentResponse(
        boolean success,
        AgentMessage message,
        String answer,
        String error,
        List<AgentEvent> events
) {
}
