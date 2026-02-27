package com.example.springboottest.modules.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * 基金历史净值实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fund_history")
public class FundHistory {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("fund_code")
    private String fundCode;

    @TableField("fund_name")
    private String fundName;

    @TableField("net_value")
    private Double netValue;

    @TableField("accumulated_value")
    private Double accumulatedValue;

    @TableField("change_percent")
    private Double changePercent;

    @TableField("trade_date")
    private LocalDate tradeDate;

    @TableField("create_time")
    private LocalDate createTime;
}
