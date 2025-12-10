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
@TableName("user_stock_watch")
public class UserStockWatch {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("stock_id")
    private Long stockId;

    @TableField("watch_time")
    private LocalDateTime watchTime;

    @TableField("alert_enabled")
    private Integer alertEnabled;

    @TableField("alert_price")
    private BigDecimal alertPrice;

    @TableField("alert_type")
    private String alertType;

    @TableField("notes")
    private String notes;
}
