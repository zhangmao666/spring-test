package com.example.springboottest.modules.ai.agent.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentEventRecord {

    private Long id;
    private String eventId;
    private String runId;
    private String conversationId;
    private String eventType;
    private String payload;
    private Integer turnIndex;
    private LocalDateTime createdAt;
}
