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
public class UserStockWatchDTO {

    private Long id;

    private Long userId;

    private Long stockId;

    private String stockCode;

    private String stockName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime watchTime;

    private Integer alertEnabled;

    private BigDecimal alertPrice;

    private String alertType;

    private String notes;

    private StockQuoteDTO latestQuote;

    private StockAnalysisDTO latestAnalysis;
}
