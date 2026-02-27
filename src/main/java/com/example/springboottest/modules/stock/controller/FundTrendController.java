package com.example.springboottest.modules.stock.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.stock.dto.FundAnalysisDTO;
import com.example.springboottest.modules.stock.dto.FundPredictionDTO;
import com.example.springboottest.modules.stock.dto.FundTrendDTO;
import com.example.springboottest.modules.stock.service.FundAnalysisService;
import com.example.springboottest.modules.stock.service.FundPredictionService;
import com.example.springboottest.modules.stock.service.FundTrendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 基金走势和预测Controller
 */
@Slf4j
@RestController
@RequestMapping("/fund")
@RequiredArgsConstructor
public class FundTrendController {

    private final FundTrendService fundTrendService;
    private final FundPredictionService fundPredictionService;
    private final FundAnalysisService fundAnalysisService;

    /**
     * 获取基金走势数据
     * @param fundCode 基金代码
     * @param days 天数（默认90天）
     */
    @GetMapping("/trend/{fundCode}")
    public ApiResponse<FundTrendDTO> getFundTrend(
            @PathVariable String fundCode,
            @RequestParam(required = false, defaultValue = "90") Integer days) {
        log.info("获取基金走势: fundCode={}, days={}", fundCode, days);
        FundTrendDTO trend = fundTrendService.getFundTrend(fundCode, days);
        return ApiResponse.success(trend);
    }

    /**
     * 获取基金AI预测
     * @param fundCode 基金代码
     */
    @GetMapping("/predict/{fundCode}")
    public ApiResponse<FundPredictionDTO> predictFundTrend(@PathVariable String fundCode) {
        log.info("预测基金走势: fundCode={}", fundCode);
        FundPredictionDTO prediction = fundPredictionService.predictFundTrend(fundCode);
        return ApiResponse.success(prediction);
    }

    /**
     * 刷新基金历史数据
     * @param fundCode 基金代码
     * @param days 天数（默认90天）
     */
    @PostMapping("/refresh/{fundCode}")
    public ApiResponse<String> refreshFundData(
            @PathVariable String fundCode,
            @RequestParam(required = false, defaultValue = "90") Integer days) {
        log.info("刷新基金数据: fundCode={}, days={}", fundCode, days);
        fundTrendService.fetchAndSaveFundHistory(fundCode, days);
        return ApiResponse.success("基金数据刷新成功");
    }

    /**
     * 获取基金走势摘要（用于AI分析页面的基础数据展示）
     * @param fundCode 基金代码
     * @param days 天数（默认90天）
     */
    @GetMapping("/analysis/summary/{fundCode}")
    public ApiResponse<FundAnalysisDTO> getAnalysisSummary(
            @PathVariable String fundCode,
            @RequestParam(required = false, defaultValue = "90") Integer days) {
        log.info("获取基金走势摘要: fundCode={}, days={}", fundCode, days);
        FundAnalysisDTO summary = fundAnalysisService.getTrendSummary(fundCode, days);
        return ApiResponse.success(summary);
    }

    /**
     * AI实时分析基金走势（SSE流式输出）
     * @param fundCode 基金代码
     * @param days 天数（默认90天）
     */
    @GetMapping(value = "/analysis/stream/{fundCode}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyzeStream(
            @PathVariable String fundCode,
            @RequestParam(required = false, defaultValue = "90") Integer days) {
        log.info("AI实时分析基金走势: fundCode={}, days={}", fundCode, days);
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时
        fundAnalysisService.analyzeStream(fundCode, days, emitter);
        return emitter;
    }
}
