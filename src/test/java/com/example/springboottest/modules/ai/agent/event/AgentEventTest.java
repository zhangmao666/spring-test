package com.example.springboottest.modules.ai.agent.event;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentEventTest {

    @Test
    void shouldMapNewRuntimeEventsToLegacySseNames() {
        assertEquals("agent_todo", AgentEvent.of("run", "thread", AgentEventType.TODO_UPDATED, Map.of()).toLegacySseEvent().name());
        assertEquals("agent_compact", AgentEvent.of("run", "thread", AgentEventType.CONTEXT_COMPACTED, Map.of()).toLegacySseEvent().name());
        assertEquals("agent_permission", AgentEvent.of("run", "thread", AgentEventType.PERMISSION_REQUESTED, Map.of()).toLegacySseEvent().name());
    }
}
