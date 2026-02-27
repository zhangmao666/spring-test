package com.example.springboottest.modules.ai.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {
    
    private Long id;
    
    private String role;
    
    private String content;
    
    private String thought;
    
    private Integer thinkingTime;
    
    private Boolean usedWebSearch;
    
    private Boolean usedDeepThinking;
    
    private LocalDateTime timestamp;
    
    private Integer messageIndex;
}
