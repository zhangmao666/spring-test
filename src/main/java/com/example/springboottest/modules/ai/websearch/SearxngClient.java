package com.example.springboottest.modules.ai.websearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SearxngClient {

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final WebSearchProperties webSearchProperties;

    public List<SearxngResult> search(String query) throws IOException {
        return search(query, null, null);
    }

    public List<SearxngResult> search(String query, String categories, String timeRange) throws IOException {
        HttpUrl baseUrl = HttpUrl.parse(trimTrailingSlash(webSearchProperties.getSearxng().getBaseUrl()) + "/search");
        if (baseUrl == null) {
            throw new IOException("Invalid SearXNG base URL");
        }

        HttpUrl.Builder urlBuilder = baseUrl.newBuilder()
                .addQueryParameter("q", query)
                .addQueryParameter("format", "json")
                .addQueryParameter("language", webSearchProperties.getSearxng().getLanguage())
                .addQueryParameter("safesearch", String.valueOf(webSearchProperties.getSearxng().getSafeSearch()));
        if (StringUtils.hasText(categories)) {
            urlBuilder.addQueryParameter("categories", categories);
        }
        if (StringUtils.hasText(timeRange)) {
            urlBuilder.addQueryParameter("time_range", timeRange);
        }
        HttpUrl url = urlBuilder.build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = okHttpClient.newBuilder()
                .callTimeout(webSearchProperties.getSearxng().getTimeoutMs(), TimeUnit.MILLISECONDS)
                .build()
                .newCall(request)
                .execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("SearXNG request failed: " + response.code());
            }
            String body = response.body() == null ? "" : response.body().string();
            JsonNode root = objectMapper.readTree(body);
            JsonNode resultsNode = root.path("results");
            List<SearxngResult> results = new ArrayList<>();
            if (!resultsNode.isArray()) {
                return results;
            }
            for (JsonNode item : resultsNode) {
                String urlValue = text(item, "url");
                if (!StringUtils.hasText(urlValue)) {
                    continue;
                }
                results.add(SearxngResult.builder()
                        .title(text(item, "title"))
                        .url(urlValue)
                        .content(firstNonBlank(text(item, "content"), text(item, "snippet")))
                        .engine(text(item, "engine"))
                        .build());
            }
            return results;
        }
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode child = node.path(fieldName);
        return child.isMissingNode() || child.isNull() ? null : child.asText(null);
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    @Builder
    public record SearxngResult(
            String title,
            String url,
            String content,
            String engine
    ) {
    }
}
