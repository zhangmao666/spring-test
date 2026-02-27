package com.example.springboottest.modules.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 每日资讯实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("daily_news")
public class DailyNews {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("source")
    private String source;

    @TableField("url")
    private String url;

    @TableField("category")
    private String category; // 国内/国际/科技/财经等

    @TableField("publish_time")
    private LocalDateTime publishTime;

    @TableField("create_time")
    private LocalDateTime createTime;
}
