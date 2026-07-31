package com.example.springboottest.entity.DTO;

import com.example.springboottest.modules.ai.dto.SearchStatus;
import com.example.springboottest.modules.ai.dto.WebSearchSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    private Boolean usedWebSearch;

    private String searchQuery;

    private SearchStatus searchStatus;

    private List<WebSearchSource> sources;

    private boolean success;

    private String error;

    private String agentRunId;

    private String agentStatus;
}
