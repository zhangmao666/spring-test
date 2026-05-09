package com.example.springboottest.modules.news.service;

import com.example.springboottest.modules.news.dto.HotNewsItemResponse;
import com.example.springboottest.modules.news.dto.HotNewsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotboardServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private HotboardService hotboardService;

    @Test
    void shouldMapSupportedPlatformResponse() {
        Map<String, Object> responseBody = Map.of(
                "type", "tencent-news",
                "update_time", "2026-03-27T01:32:13.112Z",
                "list", List.of(
                        Map.of(
                                "index", 1,
                                "title", "第一条热点",
                                "url", "https://example.com/1",
                                "hot_value", "15120",
                                "extra", Map.of("source", List.of("每日经济新闻"))
                        ),
                        Map.of(
                                "index", 2,
                                "title", "第二条热点",
                                "url", "https://example.com/2",
                                "hot_value", "",
                                "extra", Map.of()
                        )
                )
        );

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(responseBody));

        HotNewsResponse result = hotboardService.getHotNews("tencent-news");

        assertEquals("tencent-news", result.getPlatform());
        assertEquals("腾讯新闻", result.getPlatformName());
        assertEquals("2026-03-27T01:32:13.112Z", result.getUpdateTime());
        assertEquals(2, result.getItems().size());

        HotNewsItemResponse firstItem = result.getItems().get(0);
        assertEquals(1, firstItem.getRank());
        assertEquals("第一条热点", firstItem.getTitle());
        assertEquals("https://example.com/1", firstItem.getUrl());
        assertEquals("15120", firstItem.getHotValue());
        assertEquals("每日经济新闻", firstItem.getSource());

        HotNewsItemResponse secondItem = result.getItems().get(1);
        assertEquals("", secondItem.getHotValue());
        assertEquals("腾讯新闻", secondItem.getSource());
    }

    @Test
    void shouldUseDefaultPlatformWhenPlatformIsBlank() {
        Map<String, Object> responseBody = Map.of(
                "type", "thepaper",
                "update_time", "2026-03-27T01:10:26.232Z",
                "list", List.of()
        );

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(responseBody));

        HotNewsResponse result = hotboardService.getHotNews(" ");

        assertEquals("thepaper", result.getPlatform());
        assertEquals("澎湃新闻", result.getPlatformName());
    }

    @Test
    void shouldRejectUnsupportedPlatform() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> hotboardService.getHotNews("business")
        );

        assertEquals("不支持的平台: business", exception.getMessage());
    }

    @Test
    void shouldHandleMissingListStructure() {
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(Map.of("type", "thepaper")));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> hotboardService.getHotNews("thepaper")
        );

        assertTrue(exception.getMessage().contains("获取热点新闻失败"));
        assertTrue(exception.getMessage().contains("上游返回数据结构异常"));
    }

    @Test
    void shouldFallbackToPlatformNameWhenExtraSourceMissing() {
        HotNewsItemResponse result = HotboardService.mapItem(
                Map.of(
                        "index", "7",
                        "title", "测试标题",
                        "url", "https://example.com/news",
                        "hot_value", "435万",
                        "extra", Map.of()
                ),
                "新浪新闻"
        );

        assertEquals(7, result.getRank());
        assertEquals("435万", result.getHotValue());
        assertEquals("新浪新闻", result.getSource());
    }
}
