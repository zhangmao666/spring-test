package com.example.springboottest.modules.ai.websearch;

import com.example.springboottest.modules.ai.dto.SearchStatus;
import com.example.springboottest.modules.ai.dto.WebSearchSource;
import lombok.Builder;

import java.util.List;

@Builder
public record WebSearchContext(
        boolean requested,
        boolean success,
        boolean abortChat,
        String errorMessage,
        String searchQuery,
        SearchStatus searchStatus,
        String promptContext,
        List<WebSearchSource> sources
) {
}
