package com.example.springboottest.modules.news.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.news.dto.HotNewsResponse;
import com.example.springboottest.modules.news.entity.DailyNews;
import com.example.springboottest.modules.news.repository.DailyNewsRepository;
import com.example.springboottest.modules.news.service.DailyNewsPushService;
import com.example.springboottest.modules.news.service.HotboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * News endpoints for daily digest and hotboard data.
 */
@Slf4j
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {

    private static final String SUBSCRIBE_SUCCESS = "\u8ba2\u9605\u6210\u529f";
    private static final String PUSH_STARTED = "\u63a8\u9001\u4efb\u52a1\u5df2\u542f\u52a8";
    private static final String FETCH_STARTED = "\u6293\u53d6\u4efb\u52a1\u5df2\u542f\u52a8";

    private final DailyNewsPushService dailyNewsPushService;
    private final DailyNewsRepository dailyNewsRepository;
    private final HotboardService hotboardService;

    @GetMapping("/list")
    public ApiResponse<List<DailyNews>> getNewsList() {
        log.info("Loading daily news list");
        LocalDateTime startOfDay = LocalDate.now().minusDays(7).atStartOfDay();
        List<DailyNews> newsList = dailyNewsRepository.selectList(
                new LambdaQueryWrapper<DailyNews>()
                        .ge(DailyNews::getCreateTime, startOfDay)
                        .orderByDesc(DailyNews::getPublishTime)
                        .last("LIMIT 50")
        );
        if (newsList.isEmpty()) {
            log.info("No daily news found in recent days, fetching fresh data");
            newsList = dailyNewsPushService.fetchTodayNews();
        }
        return ApiResponse.success(newsList);
    }

    @PostMapping("/subscribe")
    public ApiResponse<String> subscribe(@RequestParam String email, @RequestParam String username) {
        log.info("Adding news subscriber: email={}, username={}", email, username);
        dailyNewsPushService.addSubscriber(email, username);
        return ApiResponse.success(SUBSCRIBE_SUCCESS);
    }

    @PostMapping("/push")
    public ApiResponse<String> manualPush() {
        log.info("Triggering manual daily news push");
        new Thread(dailyNewsPushService::pushNewsToSubscribers).start();
        return ApiResponse.success(PUSH_STARTED);
    }

    @PostMapping("/fetch")
    public ApiResponse<String> manualFetch() {
        log.info("Triggering manual daily news fetch");
        new Thread(dailyNewsPushService::fetchTodayNews).start();
        return ApiResponse.success(FETCH_STARTED);
    }

    @GetMapping("/hot")
    public ApiResponse<HotNewsResponse> getHotNews(
            @RequestParam(defaultValue = HotboardService.DEFAULT_PLATFORM) String platform) {
        log.info("Loading hotboard news: platform={}", platform);
        try {
            HotNewsResponse result = hotboardService.getHotNews(platform);
            return ApiResponse.success(result);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
