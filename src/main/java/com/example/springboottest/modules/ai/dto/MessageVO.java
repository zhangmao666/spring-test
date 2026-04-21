package com.example.springboottest.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    private String searchQuery;

    private SearchStatus searchStatus;

    private List<WebSearchSource> sources;

    private LocalDateTime timestamp;

    private Integer messageIndex;
}
