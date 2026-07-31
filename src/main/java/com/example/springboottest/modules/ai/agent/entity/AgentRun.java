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
public class AgentRun {

    private Long id;
    private String runId;
    private String conversationId;
    private String parentRunId;
    private String agentName;
    private String agentMode;
    private String permissionMode;
    private String provider;
    private String model;
    private String status;
    private Integer maxTurns;
    private Integer currentTurn;
    private String resumeFromRunId;
    private String summary;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
