package com.example.springboottest.config;

import org.springframework.context.annotation.Configuration;

/**
 * 限流配置类
 * 
 * 注意: 要启用限流功能，需要在pom.xml中添加bucket4j依赖:
 * <dependency>
 *     <groupId>com.github.vladimir-bukhtoyarov</groupId>
 *     <artifactId>bucket4j-core</artifactId>
 *     <version>8.1.1</version>
 * </dependency>
 * 
 * 然后在WebConfig的addInterceptors方法中添加RateLimitInterceptor
 */
@Configuration
public class RateLimitConfig {
    // 配置类，用于文档说明
}
