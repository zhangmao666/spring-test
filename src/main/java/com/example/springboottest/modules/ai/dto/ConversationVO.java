package com.example.springboottest.modules.ai.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话列表响应DTO
 */
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
    
    private LocalDateTime lastMessageTime;
    
    private Integer messageCount;
    
    private LocalDateTime createdAt;
}
