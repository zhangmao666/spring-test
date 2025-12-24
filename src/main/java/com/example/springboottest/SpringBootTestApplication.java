package com.example.springboottest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring Boot 主启动类
 * 
 * @author Generated
 * @version 1.0
 */
@SpringBootApplication
@EnableCaching  // 启用 Spring Cache 缓存功能
@EnableAspectJAutoProxy
@EnableAsync  // 启用异步任务支持（用于异步记录日志）
@MapperScan({"com.example.springboottest.repository", "com.example.springboottest.modules.*.repository"})
public class SpringBootTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootTestApplication.class, args);
        System.out.println("Spring Boot 应用启动成功！");
    }
}
