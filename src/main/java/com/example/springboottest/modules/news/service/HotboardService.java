package com.example.springboottest.modules.news.service;

import com.example.springboottest.modules.news.dto.HotNewsItemResponse;
import com.example.springboottest.modules.news.dto.HotNewsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotboardService {

    public static final String DEFAULT_PLATFORM = "thepaper";
    private static final String HOTBOARD_API_URL = "https://uapis.cn/api/v1/misc/hotboard";
    private static final String INVALID_PLATFORM_MESSAGE = "\u4e0d\u652f\u6301\u7684\u5e73\u53f0: ";
    private static final String INVALID_RESPONSE_MESSAGE = "\u4e0a\u6e38\u8fd4\u56de\u6570\u636e\u7ed3\u6784\u5f02\u5e38";
    private static final String FETCH_FAILED_MESSAGE = "\u83b7\u53d6\u70ed\u70b9\u65b0\u95fb\u5931\u8d25: ";

    private static final Map<String, String> SUPPORTED_PLATFORMS = createSupportedPlatforms();

    private final RestTemplate restTemplate;

    public HotNewsResponse getHotNews(String platform) {
        String platformId = normalizePlatform(platform);
        String platformName = SUPPORTED_PLATFORMS.get(platformId);

        URI uri = UriComponentsBuilder.fromUriString(HOTBOARD_API_URL)
                .queryParam("type", platformId)
                .build(true)
                .toUri();

        log.info("Fetching hotboard news from UAPI: platform={}", platformId);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );
            Map<String, Object> body = response.getBody();

            if (body == null || !(body.get("list") instanceof List<?> rawItems)) {
                throw new IllegalStateException(INVALID_RESPONSE_MESSAGE);
            }

            List<HotNewsItemResponse> items = rawItems.stream()
                    .filter(Map.class::isInstance)
                    .map(Map.class::cast)
                    .map(item -> mapItem(item, platformName))
                    .filter(Objects::nonNull)
                    .toList();

            return HotNewsResponse.builder()
                    .platform(platformId)
                    .platformName(platformName)
                    .updateTime(asString(body.get("update_time")))
                    .items(items)
                    .build();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch hotboard news from UAPI: platform={}, reason={}", platformId, e.getMessage(), e);
            throw new RuntimeException(FETCH_FAILED_MESSAGE + e.getMessage(), e);
        }
    }

    static String normalizePlatform(String platform) {
        String normalized = StringUtils.hasText(platform) ? platform.trim() : DEFAULT_PLATFORM;
        if (!SUPPORTED_PLATFORMS.containsKey(normalized)) {
            throw new IllegalArgumentException(INVALID_PLATFORM_MESSAGE + normalized);
        }
        return normalized;
    }

    static HotNewsItemResponse mapItem(Map<String, Object> item, String fallbackSource) {
        if (item == null) {
            return null;
        }

        return HotNewsItemResponse.builder()
                .rank(asInteger(item.get("index")))
                .title(asString(item.get("title")))
                .url(asString(item.get("url")))
                .hotValue(asString(item.get("hot_value")))
                .source(extractSource(item.get("extra"), fallbackSource))
                .build();
    }

    private static String extractSource(Object extra, String fallbackSource) {
        if (extra instanceof Map<?, ?> extraMap) {
            Object source = extraMap.get("source");
            if (source instanceof List<?> sourceList && !sourceList.isEmpty()) {
                Object first = sourceList.get(0);
                if (first != null && StringUtils.hasText(first.toString())) {
                    return first.toString();
                }
            }
            if (source != null && StringUtils.hasText(source.toString())) {
                return source.toString();
            }
        }
        return fallbackSource;
    }

    private static Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static Map<String, String> createSupportedPlatforms() {
        Map<String, String> platforms = new LinkedHashMap<>();
        platforms.put("thepaper", "\u6f8e\u6e43\u65b0\u95fb");
        platforms.put("toutiao", "\u4eca\u65e5\u5934\u6761");
        platforms.put("tencent-news", "\u817e\u8baf\u65b0\u95fb");
        platforms.put("sina-news", "\u65b0\u6d6a\u65b0\u95fb");
        platforms.put("netease-news", "\u7f51\u6613\u65b0\u95fb");
        return Collections.unmodifiableMap(platforms);
    }
}
