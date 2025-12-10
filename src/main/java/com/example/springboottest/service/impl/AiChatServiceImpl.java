package com.example.springboottest.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.example.springboottest.config.AiProperties;
import com.example.springboottest.DTO.AiChatRequest;
import com.example.springboottest.DTO.AiChatResponse;
import com.example.springboottest.DTO.AiProviderInfo;
import com.example.springboottest.service.AiChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiProperties aiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, String> conversationContexts = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<AiChatResponse> chat(AiChatRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            String provider = (request.getProvider() != null && !request.getProvider().trim().isEmpty())
                ? request.getProvider()
                : aiProperties.getDefaultProvider();

            log.info("处理AI聊天请求，提供商: {}, 消息: {}", provider, request.getMessage());

            return switch (provider.toLowerCase()) {
                case "openai" -> chatWithOpenAi(request, startTime);
                case "qwen" -> chatWithQwen(request, startTime);
                case "wenxin" -> chatWithWenxin(request, startTime);
                default -> AiChatResponse.builder()
                    .success(false)
                    .error("不支持的AI提供商: " + provider)
                    .responseTime(System.currentTimeMillis() - startTime)
                    .build();
            };
        });
    }

    private AiChatResponse chatWithOpenAi(AiChatRequest request, long startTime) {
        if (aiProperties.getOpenai().getApiKey() == null || aiProperties.getOpenai().getApiKey().trim().isEmpty()) {
            return AiChatResponse.builder()
                .success(false)
                .error("OpenAI API密钥未配置")
                .responseTime(System.currentTimeMillis() - startTime)
                .build();
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", request.getModel() != null ? request.getModel() : aiProperties.getOpenai().getModel());
            requestBody.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : aiProperties.getOpenai().getMaxTokens());
            requestBody.put("temperature", request.getTemperature() != null ? request.getTemperature() : aiProperties.getOpenai().getTemperature());

            List<Map<String, String>> messages = new ArrayList<>();

            // 添加历史对话上下文
            if (request.getConversationId() != null && !request.getConversationId().trim().isEmpty()) {
                String context = conversationContexts.get(request.getConversationId());
                if (context != null && !context.trim().isEmpty()) {
                    messages.add(Map.of("role", "system", "content", context));
                }
            }

            messages.add(Map.of("role", "user", "content", request.getMessage()));
            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiProperties.getOpenai().getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                aiProperties.getOpenai().getApiUrl(),
                HttpMethod.POST,
                entity,
                String.class
            );

            return parseOpenAiResponse(response.getBody(), request, startTime);

        } catch (Exception e) {
            log.error("OpenAI请求失败", e);
            return AiChatResponse.builder()
                .success(false)
                .error("OpenAI请求失败: " + e.getMessage())
                .provider("openai")
                .responseTime(System.currentTimeMillis() - startTime)
                .build();
        }
    }

    private AiChatResponse chatWithQwen(AiChatRequest request, long startTime) {
        if (aiProperties.getQwen().getApiKey() == null || aiProperties.getQwen().getApiKey().trim().isEmpty()) {
            return AiChatResponse.builder()
                .success(false)
                .error("通义千问API密钥未配置")
                .responseTime(System.currentTimeMillis() - startTime)
                .build();
        }

        try {
            Generation gen = new Generation();

            List<Message> messages = new ArrayList<>();

            // 添加历史对话上下文
            if (request.getConversationId() != null && !request.getConversationId().trim().isEmpty()) {
                String context = conversationContexts.get(request.getConversationId());
                if (context != null && !context.trim().isEmpty()) {
                    messages.add(Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content(context)
                        .build());
                }
            }

            // 添加用户消息
            messages.add(Message.builder()
                .role(Role.USER.getValue())
                .content(request.getMessage())
                .build());

            GenerationParam param = GenerationParam.builder()
                .apiKey(aiProperties.getQwen().getApiKey())
                .model(request.getModel() != null ? request.getModel() : aiProperties.getQwen().getModel())
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .temperature(Float.valueOf(String.valueOf(
                    request.getTemperature() != null ? request.getTemperature() : aiProperties.getQwen().getTemperature())))
                .maxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : aiProperties.getQwen().getMaxTokens())
                .build();

            GenerationResult result = gen.call(param);

            if (result == null || result.getOutput() == null ||
                result.getOutput().getChoices() == null || result.getOutput().getChoices().isEmpty()) {
                return AiChatResponse.builder()
                    .success(false)
                    .error("Qwen API返回结果为空")
                    .provider("qwen")
                    .responseTime(System.currentTimeMillis() - startTime)
                    .build();
            }

            String responseMessage = result.getOutput().getChoices().get(0).getMessage().getContent();
            Integer tokensUsed = result.getUsage() != null ? result.getUsage().getTotalTokens() : null;

            // 更新对话上下文
            updateConversationContext(request.getConversationId(), request.getMessage(), responseMessage);

            return AiChatResponse.builder()
                .message(responseMessage)
                .provider("qwen")
                .model(request.getModel() != null ? request.getModel() : aiProperties.getQwen().getModel())
                .conversationId(request.getConversationId())
                .tokensUsed(tokensUsed)
                .responseTime(System.currentTimeMillis() - startTime)
                .success(true)
                .build();

        } catch (Exception e) {
            log.error("通义千问请求失败", e);
            return AiChatResponse.builder()
                .success(false)
                .error("通义千问请求失败: " + e.getMessage())
                .provider("qwen")
                .responseTime(System.currentTimeMillis() - startTime)
                .build();
        }
    }

    private AiChatResponse chatWithWenxin(AiChatRequest request, long startTime) {
        if (aiProperties.getWenxin().getApiKey() == null || aiProperties.getWenxin().getApiKey().trim().isEmpty() ||
            aiProperties.getWenxin().getSecretKey() == null || aiProperties.getWenxin().getSecretKey().trim().isEmpty()) {
            return AiChatResponse.builder()
                .success(false)
                .error("文心一言API密钥未配置")
                .responseTime(System.currentTimeMillis() - startTime)
                .build();
        }

        try {
            // 先获取access_token
            String accessToken = getWenxinAccessToken();
            if (accessToken == null || accessToken.trim().isEmpty()) {
                return AiChatResponse.builder()
                    .success(false)
                    .error("获取文心一言访问令牌失败")
                    .provider("wenxin")
                    .responseTime(System.currentTimeMillis() - startTime)
                    .build();
            }

            Map<String, Object> requestBody = new HashMap<>();

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", request.getMessage()));
            requestBody.put("messages", messages);

            if (request.getMaxTokens() != null) {
                requestBody.put("max_output_tokens", request.getMaxTokens());
            }
            if (request.getTemperature() != null) {
                requestBody.put("temperature", request.getTemperature());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = aiProperties.getWenxin().getApiUrl() + "?access_token=" + accessToken;

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            return parseWenxinResponse(response.getBody(), request, startTime);

        } catch (Exception e) {
            log.error("文心一言请求失败", e);
            return AiChatResponse.builder()
                .success(false)
                .error("文心一言请求失败: " + e.getMessage())
                .provider("wenxin")
                .responseTime(System.currentTimeMillis() - startTime)
                .build();
        }
    }

    private String getWenxinAccessToken() {
        try {
            String url = "https://aip.baidubce.com/oauth/2.0/token" +
                "?grant_type=client_credentials" +
                "&client_id=" + aiProperties.getWenxin().getApiKey() +
                "&client_secret=" + aiProperties.getWenxin().getSecretKey();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            JsonNode node = objectMapper.readTree(response.getBody());
            return node.get("access_token").asText();
        } catch (Exception e) {
            log.error("获取文心一言access_token失败", e);
            return null;
        }
    }

    private AiChatResponse parseOpenAiResponse(String response, AiChatRequest request, long startTime) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.size() > 0) {
                String message = choices.get(0).get("message").get("content").asText();
                int tokensUsed = root.get("usage").get("total_tokens").asInt();

                // 更新对话上下文
                updateConversationContext(request.getConversationId(), request.getMessage(), message);

                return AiChatResponse.builder()
                    .message(message)
                    .provider("openai")
                    .model(request.getModel())
                    .conversationId(request.getConversationId())
                    .tokensUsed(tokensUsed)
                    .responseTime(System.currentTimeMillis() - startTime)
                    .success(true)
                    .build();
            }
        } catch (Exception e) {
            log.error("解析OpenAI响应失败", e);
        }

        return AiChatResponse.builder()
            .success(false)
            .error("解析响应失败")
            .provider("openai")
            .responseTime(System.currentTimeMillis() - startTime)
            .build();
    }

    private AiChatResponse parseQwenResponse(String response, AiChatRequest request, long startTime) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode output = root.get("output");
            if (output != null) {
                String message = output.get("text").asText();

                updateConversationContext(request.getConversationId(), request.getMessage(), message);

                return AiChatResponse.builder()
                    .message(message)
                    .provider("qwen")
                    .model(request.getModel())
                    .conversationId(request.getConversationId())
                    .responseTime(System.currentTimeMillis() - startTime)
                    .success(true)
                    .build();
            }
        } catch (Exception e) {
            log.error("解析通义千问响应失败", e);
        }

        return AiChatResponse.builder()
            .success(false)
            .error("解析响应失败")
            .provider("qwen")
            .responseTime(System.currentTimeMillis() - startTime)
            .build();
    }

    private AiChatResponse parseWenxinResponse(String response, AiChatRequest request, long startTime) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String message = root.get("result").asText();

            updateConversationContext(request.getConversationId(), request.getMessage(), message);

            return AiChatResponse.builder()
                .message(message)
                .provider("wenxin")
                .model("wenxin")
                .conversationId(request.getConversationId())
                .responseTime(System.currentTimeMillis() - startTime)
                .success(true)
                .build();
        } catch (Exception e) {
            log.error("解析文心一言响应失败", e);
        }

        return AiChatResponse.builder()
            .success(false)
            .error("解析响应失败")
            .provider("wenxin")
            .responseTime(System.currentTimeMillis() - startTime)
            .build();
    }

    private void updateConversationContext(String conversationId, String userMessage, String aiMessage) {
        if (conversationId != null && !conversationId.trim().isEmpty()) {
            String context = conversationContexts.getOrDefault(conversationId, "");
            context += "用户: " + userMessage + "\nAI: " + aiMessage + "\n";
            conversationContexts.put(conversationId, context);
        }
    }

    @Override
    public List<AiProviderInfo> getAvailableProviders() {
        List<AiProviderInfo> providers = new ArrayList<>();

        providers.add(AiProviderInfo.builder()
            .provider("openai")
            .displayName("OpenAI GPT")
            .availableModels(Arrays.asList("gpt-3.5-turbo", "gpt-4", "gpt-4-turbo"))
            .available(aiProperties.getOpenai().getApiKey() != null && !aiProperties.getOpenai().getApiKey().trim().isEmpty())
            .description("OpenAI的GPT系列模型，支持多轮对话")
            .build());

        providers.add(AiProviderInfo.builder()
            .provider("qwen")
            .displayName("阿里云通义千问")
            .availableModels(Arrays.asList("qwen-turbo", "qwen-plus", "qwen-max"))
            .available(aiProperties.getQwen().getApiKey() != null && !aiProperties.getQwen().getApiKey().trim().isEmpty())
            .description("阿里云通义千问大模型，中文理解能力强")
            .build());

        providers.add(AiProviderInfo.builder()
            .provider("wenxin")
            .displayName("百度文心一言")
            .availableModels(Arrays.asList("eb-instant"))
            .available(aiProperties.getWenxin().getApiKey() != null && !aiProperties.getWenxin().getApiKey().trim().isEmpty() &&
                      aiProperties.getWenxin().getSecretKey() != null && !aiProperties.getWenxin().getSecretKey().trim().isEmpty())
            .description("百度文心一言大模型，中文对话专长")
            .build());

        return providers;
    }

    @Override
    public boolean isProviderAvailable(String provider) {
        return getAvailableProviders().stream()
            .anyMatch(p -> p.getProvider().equals(provider) && p.isAvailable());
    }
}