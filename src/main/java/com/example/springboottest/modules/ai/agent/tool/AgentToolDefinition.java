package com.example.springboottest.modules.ai.agent.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentToolDefinition {

    private String id;
    private String name;
    private String description;

    @Builder.Default
    private Map<String, Object> parametersSchema = Map.of();

    @Builder.Default
    private boolean readOnly = true;

    @Builder.Default
    private boolean approvalRequired = false;

    @Builder.Default
    private Duration timeout = Duration.ofSeconds(20);

    @Builder.Default
    private String resultSummaryStrategy = "plain_text";
}
