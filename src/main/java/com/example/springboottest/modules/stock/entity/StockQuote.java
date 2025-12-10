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
@TableName("stock_quotes")
public class StockQuote {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("stock_id")
    private Long stockId;

    @TableField("current_price")
    private BigDecimal currentPrice;

    @TableField("open_price")
    private BigDecimal openPrice;

    @TableField("close_price")
    private BigDecimal closePrice;

    @TableField("high_price")
    private BigDecimal highPrice;

    @TableField("low_price")
    private BigDecimal lowPrice;

    @TableField("volume")
    private Long volume;

    @TableField("turnover")
    private BigDecimal turnover;

    @TableField("change_amount")
    private BigDecimal changeAmount;

    @TableField("change_percent")
    private BigDecimal changePercent;

    @TableField("quote_time")
    private LocalDateTime quoteTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
