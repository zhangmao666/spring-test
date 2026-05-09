package com.example.springboottest.modules.ai.websearch;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebPageFetchServiceTest {

    private WebPageFetchService webPageFetchService;

    @BeforeEach
    void setUp() {
        WebSearchProperties properties = new WebSearchProperties();
        webPageFetchService = new WebPageFetchService(new OkHttpClient(), properties);
    }

    @Test
    void shouldRejectPrivateOrLocalUrls() {
        assertFalse(webPageFetchService.isAllowedUrl("http://127.0.0.1/test"));
        assertFalse(webPageFetchService.isAllowedUrl("http://localhost/test"));
        assertFalse(webPageFetchService.isAllowedUrl("http://192.168.1.1/test"));
        assertFalse(webPageFetchService.isAllowedUrl("file:///tmp/test"));
    }

    @Test
    void shouldAllowPublicHttpsUrls() {
        assertTrue(webPageFetchService.isAllowedUrl("https://example.com"));
    }
}
