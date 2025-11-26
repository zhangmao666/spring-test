package com.example.springboottest.modules.ai.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    private String message;
    private String provider;
    private String model;
    private String conversationId;
    private Integer tokensUsed;
    private Long responseTime;
    private boolean success;
    private String error;
}
