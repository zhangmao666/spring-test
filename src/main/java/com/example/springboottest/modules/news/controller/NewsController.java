package com.example.springboottest.modules.news.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.news.entity.DailyNews;
import com.example.springboottest.modules.news.repository.DailyNewsRepository;
import com.example.springboottest.modules.news.service.DailyNewsPushService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资讯推送管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {

    private final DailyNewsPushService dailyNewsPushService;
    private final DailyNewsRepository dailyNewsRepository;

    /**
     * 获取资讯列表
     */
    @GetMapping("/list")
    public ApiResponse<List<DailyNews>> getNewsList() {
        log.info("获取资讯列表");
        LocalDateTime startOfDay = LocalDate.now().minusDays(7).atStartOfDay();
        List<DailyNews> newsList = dailyNewsRepository.selectList(
                new LambdaQueryWrapper<DailyNews>()
                        .ge(DailyNews::getCreateTime, startOfDay)
                        .orderByDesc(DailyNews::getPublishTime)
                        .last("LIMIT 50")
        );
        return ApiResponse.success(newsList);
    }

    /**
     * 添加订阅用户
     */
    @PostMapping("/subscribe")
    public ApiResponse<String> subscribe(@RequestParam String email, @RequestParam String username) {
        log.info("添加订阅用户: email={}, username={}", email, username);
        dailyNewsPushService.addSubscriber(email, username);
        return ApiResponse.success("订阅成功");
    }

    /**
     * 手动触发推送（测试用）
     */
    @PostMapping("/push")
    public ApiResponse<String> manualPush() {
        log.info("手动触发资讯推送");
        new Thread(() -> dailyNewsPushService.pushNewsToSubscribers()).start();
        return ApiResponse.success("推送任务已启动");
    }

    /**
     * 手动获取资讯（测试用）
     */
    @PostMapping("/fetch")
    public ApiResponse<String> manualFetch() {
        log.info("手动获取资讯");
        new Thread(() -> dailyNewsPushService.fetchTodayNews()).start();
        return ApiResponse.success("获取任务已启动");
    }
}
