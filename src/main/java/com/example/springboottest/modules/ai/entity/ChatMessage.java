package com.example.springboottest.modules.ai.entity;

import com.example.springboottest.modules.ai.dto.SearchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private Long id;

    private String conversationId;

    private String role;

    private String content;

    private String thought;

    private Integer thinkingTime;

    private Boolean usedWebSearch;

    private Boolean usedDeepThinking;

    private String searchQuery;

    private SearchStatus searchStatus;

    private String sourcePayload;

    private LocalDateTime createdAt;

    private Integer messageIndex;
}
