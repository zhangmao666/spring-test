package com.example.springboottest.modules.ai.agent.tool;

import java.util.List;
import java.util.Map;

final class AgentToolSchemas {

    private AgentToolSchemas() {
    }

    static Map<String, Object> object(Map<String, Object> properties, List<String> required) {
        return Map.of(
                "type", "object",
                "properties", properties,
                "required", required == null ? List.of() : required
        );
    }

    static Map<String, Object> stringProperty(String description) {
        return Map.of("type", "string", "description", description);
    }

    static Map<String, Object> integerProperty(String description) {
        return Map.of("type", "integer", "description", description);
    }
}
