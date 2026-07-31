package com.example.springboottest.modules.ai.agent.tool;

import java.util.Collection;
import java.util.Optional;

public interface AgentToolRegistry {

    Optional<AgentToolExecutor> get(String name);

    Collection<AgentToolDefinition> definitions();
}
