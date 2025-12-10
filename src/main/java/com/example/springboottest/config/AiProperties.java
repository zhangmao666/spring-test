package com.example.springboottest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    
    private String defaultProvider = "qwen";
    private int timeout = 30;
    
    private OpenAiConfig openai = new OpenAiConfig();
    private QwenConfig qwen = new QwenConfig();
    private WenxinConfig wenxin = new WenxinConfig();
    
    @Data
    public static class OpenAiConfig {
        private String apiKey;
        private String apiUrl = "https://api.openai.com/v1/chat/completions";
        private String model = "gpt-3.5-turbo";
        private int maxTokens = 1000;
        private double temperature = 0.7;
    }
    
    @Data
    public static class QwenConfig {
        private String apiKey;
        private String apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        private String model = "qwen3-max";
        private int maxTokens = 100000;
        private double temperature = 0.7;
    }
    
    @Data
    public static class WenxinConfig {
        private String apiKey;
        private String secretKey;
        private String apiUrl = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant";
        private int maxTokens = 1000;
        private double temperature = 0.7;
    }
}