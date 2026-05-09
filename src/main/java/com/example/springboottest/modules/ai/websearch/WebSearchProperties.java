package com.example.springboottest.modules.ai.websearch;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Data
@Component
@ConfigurationProperties(prefix = "ai.web-search")
public class WebSearchProperties {

    private final Searxng searxng = new Searxng();

    private boolean fallbackToChatWithoutSearch = true;

    public boolean isWebSearchEnabled() {
        return searxng.enabled && StringUtils.hasText(searxng.baseUrl);
    }

    @Data
    public static class Searxng {

        private boolean enabled = false;

        private String baseUrl;

        private int timeoutMs = 8000;

        private int maxResults = 8;

        private int fetchTopN = 3;

        private String language = "zh-CN";

        private int safeSearch = 1;
    }
}
