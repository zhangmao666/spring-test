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
public class AgentContextSnapshot {

    private Long id;
    private String runId;
    private String conversationId;
    private Integer snapshotIndex;
    private String compactLevel;
    private Integer estimatedTokens;
    private Integer charBudget;
    private String summary;
    private String sourceBoundary;
    private LocalDateTime createdAt;
}
