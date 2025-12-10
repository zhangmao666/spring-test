package com.example.springboottest.modules.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("stocks")
public class Stock {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("stock_code")
    private String stockCode;

    @TableField("stock_name")
    private String stockName;

    @TableField("market")
    private String market;

    @TableField("industry")
    private String industry;

    @TableField("status")
    private Integer status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
