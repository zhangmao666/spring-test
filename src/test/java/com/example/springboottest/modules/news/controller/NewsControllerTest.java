package com.example.springboottest.modules.news.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.news.dto.HotNewsResponse;
import com.example.springboottest.modules.news.repository.DailyNewsRepository;
import com.example.springboottest.modules.news.service.DailyNewsPushService;
import com.example.springboottest.modules.news.service.HotboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsControllerTest {

    @Mock
    private DailyNewsPushService dailyNewsPushService;

    @Mock
    private DailyNewsRepository dailyNewsRepository;

    @Mock
    private HotboardService hotboardService;

    @InjectMocks
    private NewsController newsController;

    @Test
    void shouldReturnSuccessForValidPlatform() {
        HotNewsResponse response = HotNewsResponse.builder()
                .platform("thepaper")
                .platformName("澎湃新闻")
                .updateTime("2026-03-27T01:10:26.232Z")
                .items(List.of())
                .build();
        when(hotboardService.getHotNews("thepaper")).thenReturn(response);

        ApiResponse<HotNewsResponse> result = newsController.getHotNews("thepaper");

        assertEquals(200, result.getCode());
        assertEquals("thepaper", result.getData().getPlatform());
    }

    @Test
    void shouldWrapInvalidPlatformAs400Response() {
        when(hotboardService.getHotNews("invalid-platform"))
                .thenThrow(new IllegalArgumentException("不支持的平台: invalid-platform"));

        ApiResponse<HotNewsResponse> result = newsController.getHotNews("invalid-platform");

        assertEquals(400, result.getCode());
        assertEquals("不支持的平台: invalid-platform", result.getMessage());
    }
}
