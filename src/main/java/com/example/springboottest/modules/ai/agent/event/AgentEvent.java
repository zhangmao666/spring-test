package com.example.springboottest.modules.ai.agent.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentEvent {

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String runId;
    private String threadId;
    private AgentEventType type;

    @Builder.Default
    private Map<String, Object> payload = Map.of();

    @Builder.Default
    private Instant timestamp = Instant.now();

    public static AgentEvent of(String runId, String threadId, AgentEventType type, Map<String, Object> payload) {
        return AgentEvent.builder()
                .runId(runId)
                .threadId(threadId)
                .type(type)
                .payload(payload == null ? Map.of() : payload)
                .build();
    }

    public LegacySseEvent toLegacySseEvent() {
        Map<String, Object> data = new LinkedHashMap<>(payload == null ? Map.of() : payload);
        data.putIfAbsent("runId", runId);
        data.putIfAbsent("threadId", threadId);
        data.putIfAbsent("eventType", type == null ? null : type.name());

        if (type == AgentEventType.RUN_CREATED) {
            return new LegacySseEvent("agent_run", data);
        }
        if (type == AgentEventType.TODO_CREATED || type == AgentEventType.TODO_UPDATED) {
            return new LegacySseEvent("agent_todo", data);
        }
        if (type == AgentEventType.CONTEXT_COMPACTED) {
            return new LegacySseEvent("agent_compact", data);
        }
        if (type == AgentEventType.SUBAGENT_STARTED || type == AgentEventType.SUBAGENT_FINISHED) {
            return new LegacySseEvent("agent_subagent", data);
        }
        if (type == AgentEventType.PERMISSION_REQUESTED) {
            return new LegacySseEvent("agent_permission", data);
        }
        if (type == AgentEventType.SYSTEM_MESSAGE) {
            return new LegacySseEvent("agent_plan", data);
        }
        if (type == AgentEventType.TOOL_CALL_START) {
            return new LegacySseEvent("agent_tool_call", data);
        }
        if (type == AgentEventType.TOOL_CALL_END || type == AgentEventType.TOOL_CALL_ERROR) {
            return new LegacySseEvent("agent_tool_result", data);
        }
        return new LegacySseEvent("agent_status", data);
    }

    public record LegacySseEvent(String name, Map<String, Object> payload) {
    }
}
