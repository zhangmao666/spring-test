package com.example.springboottest.modules.stock.service;

import com.example.springboottest.modules.stock.dto.StockQuoteDTO;
import com.example.springboottest.modules.stock.entity.Stock;
import com.example.springboottest.modules.stock.entity.StockQuote;
import com.example.springboottest.modules.stock.enums.StockMarket;
import com.example.springboottest.modules.stock.repository.StockQuoteRepository;
import com.example.springboottest.modules.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDataService {

    private final OkHttpClient okHttpClient;
    private final StockRepository stockRepository;
    private final StockQuoteRepository stockQuoteRepository;

    private static final String SINA_API_URL = "https://hq.sinajs.cn/list=";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Cacheable(value = "stockQuote", key = "#stockCode", unless = "#result == null")
    public StockQuoteDTO getLatestQuote(String stockCode) {
        try {
            return fetchQuoteFromApi(stockCode);
        } catch (Exception e) {
            log.error("获取股票行情失败: {}", stockCode, e);
            Stock stock = stockRepository.findByStockCode(stockCode)
                    .orElseThrow(() -> new RuntimeException("股票不存在: " + stockCode));
            return stockQuoteRepository.findLatestByStockId(stock.getId())
                    .map(this::convertToDTO)
                    .orElse(null);
        }
    }

    public StockQuoteDTO fetchQuoteFromApi(String stockCode) throws IOException {
        String apiCode = StockMarket.getApiCode(stockCode);
        String url = SINA_API_URL + apiCode;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取股票数据失败: HTTP " + response.code());
            }

            String body = new String(response.body().bytes(), StandardCharsets.UTF_8);
            return parseQuoteData(stockCode, body);
        }
    }

    private StockQuoteDTO parseQuoteData(String stockCode, String data) {
        String[] parts = data.split("\"");
        if (parts.length < 2) {
            throw new RuntimeException("解析股票数据失败：格式不正确");
        }

        String[] fields = parts[1].split(",");
        if (fields.length < 32) {
            throw new RuntimeException("解析股票数据失败：字段不完整");
        }

        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new RuntimeException("股票不存在: " + stockCode));

        BigDecimal currentPrice = new BigDecimal(fields[3]);
        BigDecimal yesterdayClose = new BigDecimal(fields[2]);
        BigDecimal changeAmount = currentPrice.subtract(yesterdayClose);
        BigDecimal changePercent = yesterdayClose.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : changeAmount.divide(yesterdayClose, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));

        return StockQuoteDTO.builder()
                .stockId(stock.getId())
                .stockCode(stock.getStockCode())
                .stockName(fields[0])
                .currentPrice(currentPrice)
                .openPrice(new BigDecimal(fields[1]))
                .closePrice(yesterdayClose)
                .highPrice(new BigDecimal(fields[4]))
                .lowPrice(new BigDecimal(fields[5]))
                .volume(Long.parseLong(fields[8]))
                .turnover(new BigDecimal(fields[9]))
                .changeAmount(changeAmount)
                .changePercent(changePercent)
                .quoteTime(parseQuoteTime(fields[30], fields[31]))
                .build();
    }

    private LocalDateTime parseQuoteTime(String date, String time) {
        try {
            String dateTimeStr = date + " " + time;
            return LocalDateTime.parse(dateTimeStr, TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("解析行情时间失败，使用当前时间: date={}, time={}", date, time);
            return LocalDateTime.now();
        }
    }

    @Transactional
    @CacheEvict(value = "stockQuote", key = "#quote.stockCode")
    public void saveQuote(StockQuoteDTO quote) {
        StockQuote entity = StockQuote.builder()
                .stockId(quote.getStockId())
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

        stockQuoteRepository.insert(entity);
        log.debug("保存股票行情: {}", quote.getStockCode());
    }

    public List<Stock> getActiveStocks() {
        return stockRepository.findAllActive();
    }

    public List<StockQuoteDTO> batchFetchQuotes(List<String> stockCodes) {
        List<StockQuoteDTO> quotes = new ArrayList<>();
        for (String stockCode : stockCodes) {
            try {
                StockQuoteDTO quote = fetchQuoteFromApi(stockCode);
                quotes.add(quote);
            } catch (Exception e) {
                log.error("批量获取股票行情失败: {}", stockCode, e);
            }
        }
        return quotes;
    }

    private StockQuoteDTO convertToDTO(StockQuote quote) {
        Stock stock = stockRepository.selectById(quote.getStockId());
        return StockQuoteDTO.builder()
                .id(quote.getId())
                .stockId(quote.getStockId())
                .stockCode(stock != null ? stock.getStockCode() : null)
                .stockName(stock != null ? stock.getStockName() : null)
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
}
