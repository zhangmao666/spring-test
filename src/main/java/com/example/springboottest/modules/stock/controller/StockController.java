package com.example.springboottest.modules.stock.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.stock.dto.StockAnalysisDTO;
import com.example.springboottest.modules.stock.dto.StockInfoDTO;
import com.example.springboottest.modules.stock.dto.StockQuoteDTO;
import com.example.springboottest.modules.stock.dto.UserStockWatchDTO;
import com.example.springboottest.modules.stock.service.StockAnalysisService;
import com.example.springboottest.modules.stock.service.StockQuoteService;
import com.example.springboottest.modules.stock.service.StockWatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockQuoteService stockQuoteService;
    private final StockAnalysisService stockAnalysisService;
    private final StockWatchService stockWatchService;

    @GetMapping("/quote/{stockCode}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<StockQuoteDTO> getQuote(@PathVariable String stockCode) {
        log.info("获取股票行情: {}", stockCode);
        StockQuoteDTO quote = stockQuoteService.getLatestQuote(stockCode);
        return ApiResponse.success(quote);
    }

    @GetMapping("/quote/{stockCode}/history")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<List<StockQuoteDTO>> getHistory(
            @PathVariable String stockCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("获取股票历史行情: {} from {} to {}", stockCode, startDate, endDate);
        List<StockQuoteDTO> history = stockQuoteService.getHistory(stockCode, startDate, endDate);
        return ApiResponse.success(history);
    }

    @GetMapping("/analysis/{stockCode}")
    @PreAuthorize("hasRole('USER')")
    public CompletableFuture<ApiResponse<StockAnalysisDTO>> getAnalysis(@PathVariable String stockCode) {
        log.info("请求AI分析: {}", stockCode);
        return CompletableFuture.supplyAsync(() -> {
            try {
                StockQuoteDTO quote = stockQuoteService.getLatestQuote(stockCode);
                return stockAnalysisService.analyzeStock(quote.getStockId())
                        .thenApply(ApiResponse::success)
                        .join();
            } catch (Exception e) {
                log.error("AI分析失败: {}", stockCode, e);
                return ApiResponse.error("AI分析失败: " + e.getMessage());
            }
        });
    }

    @PostMapping("/watch/{stockCode}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> watchStock(@PathVariable String stockCode) {
        Long userId = getCurrentUserId();
        log.info("用户 {} 关注股票: {}", userId, stockCode);
        stockWatchService.addWatch(userId, stockCode);
        return ApiResponse.success();
    }

    @DeleteMapping("/watch/{stockCode}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> unwatchStock(@PathVariable String stockCode) {
        Long userId = getCurrentUserId();
        log.info("用户 {} 取消关注股票: {}", userId, stockCode);
        stockWatchService.removeWatch(userId, stockCode);
        return ApiResponse.success();
    }

    @GetMapping("/watch")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<List<UserStockWatchDTO>> getWatchList() {
        Long userId = getCurrentUserId();
        log.info("获取用户 {} 的关注列表", userId);
        List<UserStockWatchDTO> watchList = stockWatchService.getUserWatchList(userId);
        return ApiResponse.success(watchList);
    }

    @PostMapping("/watch/{stockCode}/alert")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> setAlert(
            @PathVariable String stockCode,
            @RequestParam BigDecimal alertPrice,
            @RequestParam String alertType) {
        Long userId = getCurrentUserId();
        log.info("用户 {} 设置价格提醒: {} - {} {}", userId, stockCode, alertType, alertPrice);
        stockWatchService.setAlert(userId, stockCode, alertPrice, alertType);
        return ApiResponse.success();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<List<StockInfoDTO>> searchStocks(@RequestParam String keyword) {
        log.info("搜索股票: {}", keyword);
        List<StockInfoDTO> stocks = stockQuoteService.searchStocks(keyword);
        return ApiResponse.success(stocks);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<List<StockInfoDTO>> getAllStocks() {
        log.info("获取所有股票列表");
        List<StockInfoDTO> stocks = stockQuoteService.getAllStocks();
        return ApiResponse.success(stocks);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails =
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            return Long.parseLong(userDetails.getUsername());
        }
        throw new RuntimeException("未找到当前用户信息");
    }
}
