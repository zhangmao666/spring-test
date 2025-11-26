package com.example.springboottest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO配置属性类
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    
    /**
     * MinIO服务端点
     */
    private String endpoint;
    
    /**
     * 访问密钥
     */
    private String accessKey;
    
    /**
     * 密钥
     */
    private String secretKey;
    
    /**
     * 存储桶名称
     */
    private String bucketName;
    
    /**
     * 是否启用HTTPS
     */
    private Boolean secure = false;
}