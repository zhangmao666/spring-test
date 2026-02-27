package com.example.springboottest.modules.news.task;

import com.example.springboottest.modules.news.service.DailyNewsPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每日资讯推送定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyNewsPushTask {

    private final DailyNewsPushService dailyNewsPushService;

    /**
     * 每天早上8点推送资讯
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void pushDailyNews() {
        log.info("开始执行每日资讯推送任务");
        try {
            dailyNewsPushService.pushNewsToSubscribers();
            log.info("每日资讯推送任务执行完成");
        } catch (Exception e) {
            log.error("每日资讯推送任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 每天凌晨1点获取最新资讯（提前准备）
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void fetchDailyNews() {
        log.info("开始获取最新资讯");
        try {
            dailyNewsPushService.fetchTodayNews();
            log.info("获取最新资讯完成");
        } catch (Exception e) {
            log.error("获取最新资讯失败: {}", e.getMessage(), e);
        }
    }
}
