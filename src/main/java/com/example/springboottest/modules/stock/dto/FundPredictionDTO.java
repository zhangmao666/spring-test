package com.example.springboottest.modules.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基金预测DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundPredictionDTO {
    private String fundCode;
    private String fundName;

    // 趋势分析
    private String trendAnalysis;

    // 预测方向：UP/DOWN/STABLE
    private String predictedDirection;

    // 置信度 0-100
    private Integer confidence;

    // 预测涨跌幅
    private Double predictedChange;

    // 风险等级：LOW/MEDIUM/HIGH
    private String riskLevel;

    // 投资建议
    private String recommendation;

    // AI详细分析
    private String detailedAnalysis;
}
