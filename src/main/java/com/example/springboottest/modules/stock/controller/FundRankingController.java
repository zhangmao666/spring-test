package com.example.springboottest.modules.stock.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.stock.entity.FundNews;
import com.example.springboottest.modules.stock.entity.FundRanking;
import com.example.springboottest.modules.stock.service.FundDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/fund")
@RequiredArgsConstructor
public class FundRankingController {

    private final FundDataService fundDataService;

    @GetMapping("/ranking")
    public ApiResponse<List<FundRanking>> getRanking() {
        return ApiResponse.success(fundDataService.getLatestRankings());
    }

    @GetMapping("/news")
    public ApiResponse<List<FundNews>> getNews() {
        return ApiResponse.success(fundDataService.getLatestNews());
    }

    @PostMapping("/update")
    public ApiResponse<String> manualUpdate() {
        log.info("手动触发基金数据更新任务");
        // 异步执行更新，避免接口超时
        new Thread(fundDataService::updateFundData).start();
        return ApiResponse.success("更新任务已启动，请稍后刷新查看数据");
    }
}
