package com.example.springboottest.modules.ai.agent.service;

import com.example.springboottest.modules.ai.agent.entity.AgentEventRecord;
import com.example.springboottest.modules.ai.agent.event.AgentEvent;
import com.example.springboottest.modules.ai.agent.mapper.AgentEventRecordMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentEventStore {

    private final AgentEventRecordMapper eventMapper;
    private final ObjectMapper objectMapper;

    public void save(AgentEvent event, Integer turnIndex) {
        if (event == null) {
            return;
        }
        try {
            eventMapper.insert(AgentEventRecord.builder()
                    .eventId(event.getId())
                    .runId(event.getRunId())
                    .conversationId(event.getThreadId())
                    .eventType(event.getType() == null ? "CUSTOM" : event.getType().name())
                    .payload(objectMapper.writeValueAsString(event.getPayload()))
                    .turnIndex(turnIndex)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to persist agent event {}", event.getId(), e);
        }
    }

    public List<AgentEventRecord> listEvents(String runId) {
        return eventMapper.findByRunId(runId);
    }
}
