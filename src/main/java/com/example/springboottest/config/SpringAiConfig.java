package com.example.springboottest.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class SpringAiConfig {

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final AiProperties aiProperties;

    public SpringAiConfig(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    /**
     * 默认的 OpenAI Chat Model（使用默认模型）
     */
    @Bean
    public OpenAiChatModel defaultOpenAiChatModel() {
        return createChatModel(aiProperties.getOpenai().getModel());
    }

    /**
     * 深度思考模型
     */
    @Bean(name = "thinkingChatModel")
    public OpenAiChatModel thinkingChatModel() {
        return createChatModel(aiProperties.getOpenai().getThinkingModel());
    }

    /**
     * 联网搜索模型
     */
    @Bean(name = "searchChatModel")
    public OpenAiChatModel searchChatModel() {
        return createChatModel(aiProperties.getOpenai().getSearchModel());
    }

    /**
     * 创建 ChatModel 的通用方法
     */
    private OpenAiChatModel createChatModel(String modelName) {
        OpenAiApi openAiApi = OpenAiApi.builder()
            .baseUrl(baseUrl)
            .apiKey(apiKey)
            .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
            .model(modelName)
            .temperature(aiProperties.getOpenai().getTemperature())
            .maxTokens(aiProperties.getOpenai().getMaxTokens())
            .build();

        return OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .defaultOptions(options)
            .build();
    }

    /**
     * 会话上下文存储（保持与原实现一致）
     */
    @Bean
    public Map<String, String> conversationContexts() {
        return new ConcurrentHashMap<>();
    }
}
