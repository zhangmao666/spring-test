package com.example.springboottest.modules.ai.agent.runtime;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ToolCallDecision {

    private String thought;
    private List<ToolCallItem> toolCalls = List.of();
    private String directAnswer;

    @Data
    public static class ToolCallItem {
        private String id;
        private String toolName;
        private Map<String, Object> arguments = Map.of();
    }
}
