package com.example.springboottest.modules.stock.task;

import com.example.springboottest.modules.stock.service.FundDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 基金数据定时抓取任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FundDataFetchTask {

    private final FundDataService fundDataService;

    /**
     * 每天凌晨 00:05 分执行一次，抓取前一天的数据
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void fetchDailyFundData() {
        log.info("定时任务执行：抓取每日基金排行与资讯");
        try {
            fundDataService.updateFundData();
            log.info("每日基金数据更新成功");
        } catch (Exception e) {
            log.error("每日基金数据更新失败", e);
        }
    }
}
