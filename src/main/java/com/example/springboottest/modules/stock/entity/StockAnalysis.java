package com.example.springboottest.modules.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("stock_analysis")
public class StockAnalysis {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("stock_id")
    private Long stockId;

    @TableField("analysis_time")
    private LocalDateTime analysisTime;

    @TableField("analysis_content")
    private String analysisContent;

    @TableField("trend_type")
    private String trendType;

    @TableField("confidence_score")
    private BigDecimal confidenceScore;

    @TableField("predicted_price")
    private BigDecimal predictedPrice;

    @TableField("risk_level")
    private String riskLevel;

    @TableField("ai_provider")
    private String aiProvider;

    @TableField("status")
    private Integer status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
