package com.example.springboottest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Spring Boot 主启动类
 * 
 * @author Generated
 * @version 1.0
 */
@SpringBootApplication
@EnableCaching  // 启用 Spring Cache 缓存功能
@EnableAspectJAutoProxy
@MapperScan({"com.example.springboottest.repository", "com.example.springboottest.modules.*.repository"})
public class SpringBootTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootTestApplication.class, args);
        System.out.println("Spring Boot 应用启动成功！");
    }
}
