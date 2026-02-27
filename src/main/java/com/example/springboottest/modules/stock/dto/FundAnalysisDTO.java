package com.example.springboottest.modules.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 基金AI实时走势分析DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundAnalysisDTO {

    private String fundCode;
    private String fundName;

    // 基础走势数据摘要
    private TrendSummary trendSummary;

    // AI分析结果（流式返回时逐步填充）
    private String analysisContent;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendSummary {
        private Double latestNetValue;
        private Double latestChange;
        private Double avgChange;
        private Double maxValue;
        private Double minValue;
        private Double volatility;
        private Double totalReturn;
        private Integer dataPoints;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<DailyPoint> recentPoints;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyPoint {
        private String date;
        private Double netValue;
        private Double changePercent;
    }
}
