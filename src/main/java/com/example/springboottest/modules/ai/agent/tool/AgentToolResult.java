package com.example.springboottest.modules.ai.agent.tool;

import lombok.Builder;

@Builder
public record AgentToolResult(
        boolean success,
        String content,
        String error
) {

    public static AgentToolResult success(String content) {
        return AgentToolResult.builder().success(true).content(content).build();
    }

    public static AgentToolResult error(String error) {
        return AgentToolResult.builder().success(false).error(error).build();
    }

    public String displayText() {
        return success ? content : error;
    }
}
