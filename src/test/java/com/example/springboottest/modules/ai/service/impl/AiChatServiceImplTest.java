package com.example.springboottest.modules.ai.service.impl;

import com.example.springboottest.modules.ai.dto.SearchStatus;
import com.example.springboottest.modules.ai.dto.WebSearchSource;
import com.example.springboottest.modules.ai.websearch.WebSearchContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiChatServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldEmbedSearchContextIntoUserMessageWhenSearchSucceeds() {
        WebSearchContext searchContext = WebSearchContext.builder()
                .requested(true)
                .success(true)
                .searchStatus(SearchStatus.SUCCESS)
                .promptContext("""
                        Search query: latest ai news

                        Source 1:
                        Title: OpenAI News
                        URL: https://example.com/news
                        Snippet: important update
                        """)
                .sources(List.of(WebSearchSource.builder()
                        .title("OpenAI News")
                        .url("https://example.com/news")
                        .snippet("important update")
                        .build()))
                .build();

        String message = AiChatServiceImpl.buildUserMessage("总结今天的 AI 新闻", searchContext);

        assertTrue(message.contains("User question:"));
        assertTrue(message.contains("总结今天的 AI 新闻"));
        assertTrue(message.contains("Use the following web search context to answer the question."));
        assertTrue(message.contains("OpenAI News"));
        assertTrue(message.contains("https://example.com/news"));
    }

    @Test
    void shouldKeepOriginalUserMessageWhenSearchContextIsUnavailable() {
        WebSearchContext searchContext = WebSearchContext.builder()
                .requested(true)
                .success(false)
                .searchStatus(SearchStatus.FALLBACK_ERROR)
                .build();

        String message = AiChatServiceImpl.buildUserMessage("总结今天的 AI 新闻", searchContext);

        assertEquals("总结今天的 AI 新闻", message);
    }

    @Test
    void shouldDisableDeepThinkingWhenAgentModeIsEnabled() {
        boolean enabled = AiChatServiceImpl.shouldEnableDeepThinking(true, true, true);

        assertTrue(!enabled);
    }

    @Test
    void shouldDisableDeepThinkingWhenModelDoesNotSupportIt() {
        boolean enabled = AiChatServiceImpl.shouldEnableDeepThinking(false, true, false);

        assertTrue(!enabled);
    }

    @Test
    void shouldEnableDeepThinkingOnlyForNonAgentThinkingRequestsOnSupportedModels() {
        boolean enabled = AiChatServiceImpl.shouldEnableDeepThinking(false, true, true);

        assertTrue(enabled);
    }

    @Test
    void shouldParseSuggestionsFromJsonArray() {
        List<String> suggestions = AiChatServiceImpl.parseFollowUpSuggestions(
                "[\"First follow-up?\",\"Second follow-up?\",\"Third follow-up?\"]",
                objectMapper
        );

        assertEquals(List.of("First follow-up?", "Second follow-up?", "Third follow-up?"), suggestions);
    }

    @Test
    void shouldParseSuggestionsFromNumberedListFallback() {
        List<String> suggestions = AiChatServiceImpl.parseFollowUpSuggestions("""
                1. What changed most recently?
                2. Which risk matters most now?
                3. What should I watch next?
                """, objectMapper);

        assertEquals(List.of(
                "What changed most recently?",
                "Which risk matters most now?",
                "What should I watch next?"
        ), suggestions);
    }

    @Test
    void shouldCleanDuplicateAndNoisySuggestions() {
        List<String> suggestions = AiChatServiceImpl.parseFollowUpSuggestions("""
                [
                  "  First follow-up?  ",
                  "",
                  "First follow-up?",
                  "Second follow-up?",
                  "This is a very long follow-up question that should be trimmed because it keeps going far beyond the allowed length for a chip in the UI."
                ]
                """, objectMapper);

        assertEquals(3, suggestions.size());
        assertEquals("First follow-up?", suggestions.get(0));
        assertEquals("Second follow-up?", suggestions.get(1));
        assertTrue(suggestions.get(2).startsWith("This is a very long follow-up question"));
        assertTrue(suggestions.get(2).length() <= 60);
    }
}
