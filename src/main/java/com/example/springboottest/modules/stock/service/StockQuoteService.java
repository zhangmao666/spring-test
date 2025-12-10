package com.example.springboottest.modules.stock.service;

import com.example.springboottest.modules.stock.dto.StockInfoDTO;
import com.example.springboottest.modules.stock.dto.StockQuoteDTO;
import com.example.springboottest.modules.stock.entity.Stock;
import com.example.springboottest.modules.stock.entity.StockQuote;
import com.example.springboottest.modules.stock.repository.StockQuoteRepository;
import com.example.springboottest.modules.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockQuoteService {

    private final StockRepository stockRepository;
    private final StockQuoteRepository stockQuoteRepository;
    private final StockDataService stockDataService;

    @Cacheable(value = "stockQuote", key = "#stockCode")
    public StockQuoteDTO getLatestQuote(String stockCode) {
        return stockDataService.getLatestQuote(stockCode);
    }

    public List<StockQuoteDTO> getHistory(String stockCode, LocalDate startDate, LocalDate endDate) {
        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new RuntimeException("股票不存在: " + stockCode));

        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        List<StockQuote> quotes = stockQuoteRepository.findByStockIdAndTimeRange(stock.getId(), startTime, endTime);

        return quotes.stream()
                .map(quote -> convertToDTO(quote, stock))
                .collect(Collectors.toList());
    }

    public List<StockInfoDTO> searchStocks(String keyword) {
        List<Stock> stocks = stockRepository.searchStocks(keyword);
        return stocks.stream()
                .map(this::convertToInfoDTO)
                .collect(Collectors.toList());
    }

    public List<StockInfoDTO> getAllStocks() {
        List<Stock> stocks = stockRepository.findAllActive();
        return stocks.stream()
                .map(this::convertToInfoDTO)
                .collect(Collectors.toList());
    }

    private StockQuoteDTO convertToDTO(StockQuote quote, Stock stock) {
        return StockQuoteDTO.builder()
                .id(quote.getId())
                .stockId(quote.getStockId())
                .stockCode(stock.getStockCode())
                .stockName(stock.getStockName())
                .currentPrice(quote.getCurrentPrice())
                .openPrice(quote.getOpenPrice())
                .closePrice(quote.getClosePrice())
                .highPrice(quote.getHighPrice())
                .lowPrice(quote.getLowPrice())
                .volume(quote.getVolume())
                .turnover(quote.getTurnover())
                .changeAmount(quote.getChangeAmount())
                .changePercent(quote.getChangePercent())
                .quoteTime(quote.getQuoteTime())
                .build();
    }

    private StockInfoDTO convertToInfoDTO(Stock stock) {
        return StockInfoDTO.builder()
                .id(stock.getId())
                .stockCode(stock.getStockCode())
                .stockName(stock.getStockName())
                .market(stock.getMarket())
                .industry(stock.getIndustry())
                .status(stock.getStatus())
                .build();
    }
}
