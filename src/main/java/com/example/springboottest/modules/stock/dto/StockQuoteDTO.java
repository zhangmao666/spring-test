package com.example.springboottest.modules.stock.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockQuoteDTO {

    private Long id;

    private Long stockId;

    private String stockCode;

    private String stockName;

    private BigDecimal currentPrice;

    private BigDecimal openPrice;

    private BigDecimal closePrice;

    private BigDecimal highPrice;

    private BigDecimal lowPrice;

    private Long volume;

    private BigDecimal turnover;

    private BigDecimal changeAmount;

    private BigDecimal changePercent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime quoteTime;
}
