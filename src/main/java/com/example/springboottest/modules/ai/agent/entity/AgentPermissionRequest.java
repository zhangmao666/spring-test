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
public class AgentPermissionRequest {

    private Long id;
    private String requestId;
    private String runId;
    private String conversationId;
    private String toolName;
    private String argumentsPayload;
    private String reason;
    private String status;
    private String decision;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
}
