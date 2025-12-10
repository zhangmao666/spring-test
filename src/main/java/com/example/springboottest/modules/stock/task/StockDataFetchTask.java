package com.example.springboottest.modules.stock.task;

import com.example.springboottest.modules.stock.config.StockProperties;
import com.example.springboottest.modules.stock.controller.StockWebSocketController;
import com.example.springboottest.modules.stock.dto.StockAnalysisDTO;
import com.example.springboottest.modules.stock.dto.StockQuoteDTO;
import com.example.springboottest.modules.stock.entity.Stock;
import com.example.springboottest.modules.stock.service.StockAnalysisService;
import com.example.springboottest.modules.stock.service.StockDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockDataFetchTask {

    private final StockDataService stockDataService;
    private final StockAnalysisService stockAnalysisService;
    private final StockWebSocketController webSocketController;
    private final StockProperties stockProperties;

//    @Scheduled(cron = "0/5 * 9-15 * * MON-FRI")
    public void fetchRealTimeQuotes() {
        if (!stockProperties.getData().getScheduleEnabled()) {
            return;
        }

        if (!isInTradingHours()) {
            log.debug("当前不在交易时段，跳过行情获取");
            return;
        }

        log.info("开始获取实时行情...");
        List<Stock> activeStocks = stockDataService.getActiveStocks();
        int successCount = 0;
        int failCount = 0;

        for (Stock stock : activeStocks) {
            try {
                StockQuoteDTO quote = stockDataService.fetchQuoteFromApi(stock.getStockCode());
                stockDataService.saveQuote(quote);
                webSocketController.pushQuote(stock.getStockCode(), quote);
                successCount++;
            } catch (Exception e) {
                log.error("获取股票行情失败: {}", stock.getStockCode(), e);
                failCount++;
            }
        }

        log.info("实时行情获取完成: 成功 {}, 失败 {}", successCount, failCount);
    }

//    @Scheduled(cron = "0 0/15 9-15 * * MON-FRI")
    public void analyzeStocks() {
        if (!stockProperties.getAnalysis().getEnabled()) {
            return;
        }

        if (!isInTradingHours()) {
            log.debug("当前不在交易时段，跳过AI分析");
            return;
        }

        log.info("开始执行股票AI分析...");
        List<Stock> activeStocks = stockDataService.getActiveStocks();
        int successCount = 0;
        int failCount = 0;

        for (Stock stock : activeStocks) {
            try {
                stockAnalysisService.analyzeStock(stock.getId())
                        .thenAccept(analysis -> {
                            if (analysis != null) {
                                webSocketController.pushAnalysis(stock.getStockCode(), analysis);
                            }
                        })
                        .exceptionally(ex -> {
                            log.error("分析股票失败: {}", stock.getStockCode(), ex);
                            return null;
                        });
                successCount++;
            } catch (Exception e) {
                log.error("触发股票分析失败: {}", stock.getStockCode(), e);
                failCount++;
            }
        }

        log.info("股票AI分析触发完成: 成功 {}, 失败 {}", successCount, failCount);
    }

//    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOldData() {
        log.info("开始清理历史数据...");
        log.info("历史数据清理完成");
    }

    private boolean isInTradingHours() {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.parse(stockProperties.getData().getTradingHours().getStart());
        LocalTime end = LocalTime.parse(stockProperties.getData().getTradingHours().getEnd());

        return now.isAfter(start) && now.isBefore(end);
    }
}
