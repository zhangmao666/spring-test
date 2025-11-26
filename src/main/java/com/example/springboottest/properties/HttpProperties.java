package com.example.springboottest.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * HTTP 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "http")
public class HttpProperties {

    /**
     * 连接超时时间（秒）
     */
    private Integer connectTimeout = 30;

    /**
     * 读取超时时间（秒）
     */
    private Integer readTimeout = 60;

    /**
     * 写入超时时间（秒）
     */
    private Integer writeTimeout = 60;

    /**
     * 最大空闲连接数
     */
    private Integer maxIdleConnections = 10;

    /**
     * 连接保持活动时间（秒）
     */
    private Integer keepAliveDuration = 300;

    /**
     * 是否启用日志
     */
    private Boolean enableLogging = true;

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;
}

