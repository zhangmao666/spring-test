package com.example.springboottest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    
    private String defaultProvider = "openai";
    private int timeout = 30;

    private OpenAiConfig openai = new OpenAiConfig();

    @Data
    public static class OpenAiConfig {
        private String apiKey;
        private String apiUrl;
        private String model;
        private String thinkingModel;
        private String searchModel;
        private Integer maxTokens;
        private Double temperature;
    }
}