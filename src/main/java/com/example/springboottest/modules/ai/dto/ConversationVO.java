package com.example.springboottest.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationVO {

    private Long id;

    private String conversationId;

    private String title;

    private String provider;

    private String model;

    private Long modelId;

    private String modelDisplayName;

    private LocalDateTime lastMessageTime;

    private Integer messageCount;

    private LocalDateTime createdAt;
}
