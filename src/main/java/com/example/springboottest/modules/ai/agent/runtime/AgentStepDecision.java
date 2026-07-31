package com.example.springboottest.modules.ai.agent.runtime;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AgentStepDecision {

    private String thought;
    private String action = "answer";
    private String answer;
    private String summary;
    private List<TodoItem> todos = List.of();
    private ToolAction tool;
    private TodoUpdate todoUpdate;
    private CompactAction compact;
    private SubagentAction subagent;

    @Data
    public static class TodoItem {
        private String todoId;
        private String content;
        private String status;
    }

    @Data
    public static class ToolAction {
        private String toolName;
        private Map<String, Object> arguments = Map.of();
    }

    @Data
    public static class TodoUpdate {
        private String todoId;
        private String status;
    }

    @Data
    public static class CompactAction {
        private String level;
        private String reason;
    }

    @Data
    public static class SubagentAction {
        private String agentType;
        private String task;
    }
}
