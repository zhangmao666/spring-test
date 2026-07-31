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
public class AgentSubagent {

    private Long id;
    private String parentRunId;
    private String childRunId;
    private String conversationId;
    private String agentType;
    private String task;
    private String status;
    private String resultSummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
