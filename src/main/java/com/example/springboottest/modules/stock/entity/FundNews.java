package com.example.springboottest.modules.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 基金资讯实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fund_news")
public class FundNews {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("title")
    private String title;
    
    @TableField("summary")
    private String summary;
    
    @TableField("url")
    private String url;
    
    @TableField("publish_date")
    private LocalDateTime publishDate;
    
    @TableField("update_date")
    private LocalDate updateDate;
}
