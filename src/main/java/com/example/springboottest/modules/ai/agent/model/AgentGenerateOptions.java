package com.example.springboottest.modules.ai.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentGenerateOptions {

    private Double temperature;
    private Integer maxTokens;
    private Integer thinkingBudget;
    private String reasoningEffort;
    private String toolChoice;
    private Boolean useDeepThinking;
}
