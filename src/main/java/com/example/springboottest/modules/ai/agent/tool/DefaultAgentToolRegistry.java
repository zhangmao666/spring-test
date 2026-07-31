package com.example.springboottest.modules.ai.agent.tool;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DefaultAgentToolRegistry implements AgentToolRegistry {

    private final Map<String, AgentToolExecutor> tools;

    public DefaultAgentToolRegistry(List<AgentToolExecutor> executors) {
        Map<String, AgentToolExecutor> registered = new LinkedHashMap<>();
        for (AgentToolExecutor executor : executors == null ? List.<AgentToolExecutor>of() : executors) {
            registered.put(executor.definition().getName(), executor);
        }
        this.tools = Map.copyOf(registered);
    }

    @Override
    public Optional<AgentToolExecutor> get(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    @Override
    public Collection<AgentToolDefinition> definitions() {
        return tools.values().stream().map(AgentToolExecutor::definition).toList();
    }
}
