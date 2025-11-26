package com.example.springboottest.config;

import com.example.springboottest.properties.HttpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * OkHttp 配置类
 * 配置全局的 OkHttpClient 实例
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class OkHttpConfig {

    private final HttpProperties httpProperties;

    /**
     * 配置 OkHttpClient Bean
     * 作为单例在整个应用中使用
     */
    @Bean
    public OkHttpClient okHttpClient() {
        log.info("初始化 OkHttpClient，配置信息: {}", httpProperties);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // 设置连接超时
        builder.connectTimeout(httpProperties.getConnectTimeout(), TimeUnit.SECONDS);
        
        // 设置读取超时
        builder.readTimeout(httpProperties.getReadTimeout(), TimeUnit.SECONDS);
        
        // 设置写入超时
        builder.writeTimeout(httpProperties.getWriteTimeout(), TimeUnit.SECONDS);

        // 配置连接池
        ConnectionPool connectionPool = new ConnectionPool(
                httpProperties.getMaxIdleConnections(),
                httpProperties.getKeepAliveDuration(),
                TimeUnit.SECONDS
        );
        builder.connectionPool(connectionPool);

        // 自动重试
        builder.retryOnConnectionFailure(true);

        // 添加日志拦截器（根据配置决定是否启用）
        if (httpProperties.getEnableLogging()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                log.debug("OkHttp: {}", message);
            });
            // 设置日志级别：NONE、BASIC、HEADERS、BODY
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        // 可以添加其他拦截器
        // builder.addInterceptor(new CustomInterceptor());

        OkHttpClient okHttpClient = builder.build();
        log.info("OkHttpClient 初始化完成");

        return okHttpClient;
    }
}

