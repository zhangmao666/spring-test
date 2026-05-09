package com.example.springboottest.modules.ai.websearch;

import com.example.springboottest.modules.ai.dto.SearchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSearchServiceTest {

    @Mock
    private SearxngClient searxngClient;

    @Mock
    private WebPageFetchService webPageFetchService;

    private WebSearchProperties properties;
    private WebSearchService webSearchService;

    @BeforeEach
    void setUp() {
        properties = new WebSearchProperties();
        properties.setFallbackToChatWithoutSearch(true);
        properties.getSearxng().setEnabled(true);
        properties.getSearxng().setBaseUrl("http://searxng.local");
        properties.getSearxng().setMaxResults(5);
        properties.getSearxng().setFetchTopN(3);
        webSearchService = new WebSearchService(properties, searxngClient, webPageFetchService);
    }

    @Test
    void shouldSkipSearchWhenFeatureNotRequested() {
        WebSearchContext context = webSearchService.prepareContext(false, "latest ai news");

        assertEquals(SearchStatus.NOT_REQUESTED, context.searchStatus());
        assertFalse(context.requested());
        verifyNoInteractions(searxngClient);
    }

    @Test
    void shouldBuildSuccessContextFromSearchResults() throws Exception {
        when(searxngClient.search("latest ai news", "news,general", "month")).thenReturn(List.of(
                SearxngClient.SearxngResult.builder()
                        .title("OpenAI News")
                        .url("https://example.com/news")
                        .content("snippet from search")
                        .build()
        ));
        when(webPageFetchService.fetch("https://example.com/news"))
                .thenReturn(WebPageFetchService.FetchResult.success("full extracted content"));

        WebSearchContext context = webSearchService.prepareContext(true, "latest ai news");

        assertTrue(context.requested());
        assertTrue(context.success());
        assertEquals(SearchStatus.SUCCESS, context.searchStatus());
        assertEquals(1, context.sources().size());
        assertTrue(context.promptContext().contains("OpenAI News"));
        assertEquals("full extracted content", context.sources().get(0).getSnippet());
    }

    @Test
    void shouldFallbackWhenSearchReturnsNoResults() throws Exception {
        when(searxngClient.search("nothing useful", null, null)).thenReturn(List.of());

        WebSearchContext context = webSearchService.prepareContext(true, "nothing useful");

        assertTrue(context.requested());
        assertFalse(context.success());
        assertEquals(SearchStatus.FALLBACK_NO_RESULT, context.searchStatus());
        assertTrue(context.sources().isEmpty());
    }

    @Test
    void shouldAbortWhenSearchFailsAndFallbackDisabled() throws Exception {
        properties.setFallbackToChatWithoutSearch(false);
        when(searxngClient.search("latest ai news", "news,general", "month")).thenThrow(new IOException("timeout"));

        WebSearchContext context = webSearchService.prepareContext(true, "latest ai news");

        assertTrue(context.requested());
        assertFalse(context.success());
        assertTrue(context.abortChat());
        assertEquals(SearchStatus.FALLBACK_ERROR, context.searchStatus());
    }

    @Test
    void shouldRewriteChineseLatestNewsQuery() {
        assertEquals("伊朗 局势 最新 新闻", WebSearchService.buildSearchQuery("最近伊朗局势如何"));
        assertEquals("news,general", WebSearchService.inferSearchCategories("最近伊朗局势如何"));
        assertEquals("month", WebSearchService.inferTimeRange("最近伊朗局势如何"));
    }

    @Test
    void shouldUseDayRangeForTodayQuery() {
        assertEquals("热点 新闻", WebSearchService.buildSearchQuery("今天热点新闻"));
        assertEquals("day", WebSearchService.inferTimeRange("今天热点新闻"));
    }
}
