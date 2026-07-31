package com.example.springboottest.modules.ai.agent.model;

import com.example.springboottest.entity.DTO.AiChatRequest;

public interface AgentModelRegistry {

    ResolvedAgentModel resolve(AiChatRequest request);

    boolean canResolve(AiChatRequest request);

    default void reset() {
    }
}
