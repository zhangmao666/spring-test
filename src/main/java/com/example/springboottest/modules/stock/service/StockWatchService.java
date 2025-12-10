package com.example.springboottest.modules.stock.service;

import com.example.springboottest.modules.stock.dto.StockAnalysisDTO;
import com.example.springboottest.modules.stock.dto.StockQuoteDTO;
import com.example.springboottest.modules.stock.dto.UserStockWatchDTO;
import com.example.springboottest.modules.stock.entity.Stock;
import com.example.springboottest.modules.stock.entity.UserStockWatch;
import com.example.springboottest.modules.stock.repository.StockRepository;
import com.example.springboottest.modules.stock.repository.UserStockWatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockWatchService {

    private final UserStockWatchRepository watchRepository;
    private final StockRepository stockRepository;
    private final StockQuoteService stockQuoteService;
    private final StockAnalysisService stockAnalysisService;

    public List<UserStockWatchDTO> getUserWatchList(Long userId) {
        List<UserStockWatch> watches = watchRepository.findByUserId(userId);
        return watches.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addWatch(Long userId, String stockCode) {
        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new RuntimeException("股票不存在: " + stockCode));

        watchRepository.findByUserIdAndStockId(userId, stock.getId())
                .ifPresent(watch -> {
                    throw new RuntimeException("已关注该股票");
                });

        UserStockWatch watch = UserStockWatch.builder()
                .userId(userId)
                .stockId(stock.getId())
                .watchTime(LocalDateTime.now())
                .alertEnabled(0)
                .build();

        watchRepository.insert(watch);
        log.info("用户 {} 关注股票: {}", userId, stockCode);
    }

    @Transactional
    public void removeWatch(Long userId, String stockCode) {
        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new RuntimeException("股票不存在: " + stockCode));

        int deleted = watchRepository.deleteByUserIdAndStockId(userId, stock.getId());
        if (deleted == 0) {
            throw new RuntimeException("未关注该股票");
        }

        log.info("用户 {} 取消关注股票: {}", userId, stockCode);
    }

    @Transactional
    public void setAlert(Long userId, String stockCode, BigDecimal alertPrice, String alertType) {
        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new RuntimeException("股票不存在: " + stockCode));

        UserStockWatch watch = watchRepository.findByUserIdAndStockId(userId, stock.getId())
                .orElseThrow(() -> new RuntimeException("未关注该股票"));

        watch.setAlertEnabled(1);
        watch.setAlertPrice(alertPrice);
        watch.setAlertType(alertType);

        watchRepository.updateById(watch);
        log.info("用户 {} 设置价格提醒: {} - {} {}", userId, stockCode, alertType, alertPrice);
    }

    public List<Long> getUserWatchStockIds(Long userId) {
        return watchRepository.findStockIdsByUserId(userId);
    }

    private UserStockWatchDTO convertToDTO(UserStockWatch watch) {
        Stock stock = stockRepository.selectById(watch.getStockId());
        if (stock == null) {
            return null;
        }

        StockQuoteDTO latestQuote = null;
        StockAnalysisDTO latestAnalysis = null;

        try {
            latestQuote = stockQuoteService.getLatestQuote(stock.getStockCode());
        } catch (Exception e) {
            log.warn("获取股票行情失败: {}", stock.getStockCode());
        }

        try {
            latestAnalysis = stockAnalysisService.getLatestAnalysis(stock.getId());
        } catch (Exception e) {
            log.warn("获取股票分析失败: {}", stock.getStockCode());
        }

        return UserStockWatchDTO.builder()
                .id(watch.getId())
                .userId(watch.getUserId())
                .stockId(watch.getStockId())
                .stockCode(stock.getStockCode())
                .stockName(stock.getStockName())
                .watchTime(watch.getWatchTime())
                .alertEnabled(watch.getAlertEnabled())
                .alertPrice(watch.getAlertPrice())
                .alertType(watch.getAlertType())
                .notes(watch.getNotes())
                .latestQuote(latestQuote)
                .latestAnalysis(latestAnalysis)
                .build();
    }
}
