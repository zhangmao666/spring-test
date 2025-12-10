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
public class StockAnalysisDTO {

    private Long id;

    private Long stockId;

    private String stockCode;

    private String stockName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime analysisTime;

    private String analysisContent;

    private String trendType;

    private BigDecimal confidenceScore;

    private BigDecimal predictedPrice;

    private String riskLevel;

    private String aiProvider;
}
