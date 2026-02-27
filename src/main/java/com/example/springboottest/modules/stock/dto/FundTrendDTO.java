package com.example.springboottest.modules.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * 基金走势DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTrendDTO {
    private String fundCode;
    private String fundName;
    private List<TrendPoint> trendData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private LocalDate date;
        private Double netValue;
        private Double accumulatedValue;
        private Double changePercent;
    }
}
