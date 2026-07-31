package com.example.springboottest.modules.ai.agent.tool;

import java.util.Map;

public interface AgentToolExecutor {

    AgentToolDefinition definition();

    AgentToolResult execute(Map<String, Object> arguments);
}
