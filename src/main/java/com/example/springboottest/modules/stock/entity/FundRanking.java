package com.example.springboottest.modules.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * 基金排行实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fund_ranking")
public class FundRanking {
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

    @TableField("one_week_return")
    private Double oneWeekReturn;

    @TableField("one_month_return")
    private Double oneMonthReturn;

    @TableField("three_month_return")
    private Double threeMonthReturn;

    @TableField("six_month_return")
    private Double sixMonthReturn;

    @TableField("one_year_return")
    private Double oneYearReturn;

    @TableField("ytd_return")
    private Double ytdReturn;

    @TableField("since_inception_return")
    private Double sinceInceptionReturn;

    @TableField("max_drawdown")
    private Double maxDrawdown;

    @TableField("sharpe_ratio")
    private Double sharpeRatio;

    @TableField("manager_name")
    private String managerName;

    @TableField("manager_years")
    private Integer managerYears;

    @TableField("star_rating")
    private Integer starRating;

    @TableField("fund_type")
    private String fundType;

    @TableField("sector")
    private String sector;
    
    @TableField("update_date")
    private LocalDate updateDate;
}
