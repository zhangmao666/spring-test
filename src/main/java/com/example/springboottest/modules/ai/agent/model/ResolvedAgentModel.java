package com.example.springboottest.modules.ai.agent.model;

import lombok.Builder;
import org.springframework.ai.chat.model.ChatModel;

@Builder
public record ResolvedAgentModel(
        boolean available,
        boolean managed,
        ChatModel chatModel,
        String provider,
        String modelName,
        String displayName,
        Long modelId,
        String baseUrl,
        String apiKey,
        AgentGenerateOptions options,
        String errorMessage
) {

    public static ResolvedAgentModel unavailable(String errorMessage) {
        return ResolvedAgentModel.builder()
                .available(false)
                .errorMessage(errorMessage)
                .build();
    }
}
