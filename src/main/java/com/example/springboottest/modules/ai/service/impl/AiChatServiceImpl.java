package com.example.springboottest.modules.ai.service.impl;

import com.example.springboottest.config.AiProperties;
import com.example.springboottest.entity.DTO.AiChatRequest;
import com.example.springboottest.entity.DTO.AiChatResponse;
import com.example.springboottest.entity.DTO.AiProviderInfo;
import com.example.springboottest.modules.ai.service.AiChatService;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

            // 强制使用 openai
            String provider = "openai";

            log.info("处理AI聊天请求，提供商: {}, 消息: {}", provider, request.getMessage());

            return chatWithOpenAi(request, startTime);
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
            
            String model = aiProperties.getOpenai().getModel();
            if (Boolean.TRUE.equals(request.getUseDeepThinking()) && aiProperties.getOpenai().getThinkingModel() != null) {
                model = aiProperties.getOpenai().getThinkingModel();
            } else if (Boolean.TRUE.equals(request.getUseWebSearch()) && aiProperties.getOpenai().getSearchModel() != null) {
                model = aiProperties.getOpenai().getSearchModel();
            }
            
            requestBody.put("model", model);
            requestBody.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : aiProperties.getOpenai().getMaxTokens());
            requestBody.put("temperature", request.getTemperature() != null ? request.getTemperature() : aiProperties.getOpenai().getTemperature());

            if (Boolean.TRUE.equals(request.getUseWebSearch())) {
                requestBody.put("tools", List.of(Map.of("type", "web_search")));
            }

            List<Map<String, String>> messages = new ArrayList<>();

            if (request.getConversationId() != null && !request.getConversationId().trim().isEmpty()) {
                String context = conversationContexts.get(request.getConversationId());
                if (context != null && !context.trim().isEmpty()) {
                    messages.add(Map.of("role", "system", "content", context));
                }
            }

            // 提示词注入已移除，由特定模型处理深度思考和联网搜索

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

    private AiChatResponse parseOpenAiResponse(String response, AiChatRequest request, long startTime) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.size() > 0) {
                String message = choices.get(0).get("message").get("content").asText();
                int tokensUsed = root.get("usage").get("total_tokens").asInt();

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
            .availableModels(Collections.singletonList(aiProperties.getOpenai().getModel()))
            .available(aiProperties.getOpenai().getApiKey() != null && !aiProperties.getOpenai().getApiKey().trim().isEmpty())
            .description("OpenAI的GPT系列模型，支持多轮对话")
            .build());

        return providers;
    }

    @Override
    public boolean isProviderAvailable(String provider) {
        return "openai".equalsIgnoreCase(provider) && 
               aiProperties.getOpenai().getApiKey() != null && 
               !aiProperties.getOpenai().getApiKey().trim().isEmpty();
    }

    @Override
    public void chatStream(AiChatRequest request, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            try {
                // 强制使用 openai
                chatStreamWithOpenAi(request, emitter);
            } catch (Exception e) {
                log.error("流式聊天请求处理失败", e);
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"error\":\"" + e.getMessage() + "\"}"));
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error("发送错误消息失败", ex);
                }
            }
        });
    }

    private void chatStreamWithOpenAi(AiChatRequest request, SseEmitter emitter) {
        if (aiProperties.getOpenai().getApiKey() == null || aiProperties.getOpenai().getApiKey().trim().isEmpty()) {
            try {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"error\":\"OpenAI API密钥未配置\"}"));
                emitter.complete();
            } catch (Exception e) {
                log.error("发送错误消息失败", e);
            }
            return;
        }

        try {
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

            Map<String, Object> requestBody = new HashMap<>();
            
            String model = aiProperties.getOpenai().getModel();
            if (Boolean.TRUE.equals(request.getUseDeepThinking()) && aiProperties.getOpenai().getThinkingModel() != null) {
                model = aiProperties.getOpenai().getThinkingModel();
            } else if (Boolean.TRUE.equals(request.getUseWebSearch()) && aiProperties.getOpenai().getSearchModel() != null) {
                model = aiProperties.getOpenai().getSearchModel();
            }
            
            requestBody.put("model", model);
            requestBody.put("stream", true);

            if (Boolean.TRUE.equals(request.getUseWebSearch())) {
                requestBody.put("tools", List.of(Map.of("type", "web_search")));
            }

            List<Map<String, String>> messages = new ArrayList<>();

            if (request.getConversationId() != null && !request.getConversationId().trim().isEmpty()) {
                String context = conversationContexts.get(request.getConversationId());
                if (context != null && !context.trim().isEmpty()) {
                    messages.add(Map.of("role", "system", "content", context));
                }
            }

            // 提示词注入已移除，由特定模型处理深度思考和联网搜索

            messages.add(Map.of("role", "user", "content", request.getMessage()));
            requestBody.put("messages", messages);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            okhttp3.Request httpRequest = new okhttp3.Request.Builder()
                .url(aiProperties.getOpenai().getApiUrl())
                .addHeader("Authorization", "Bearer " + aiProperties.getOpenai().getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(okhttp3.RequestBody.create(jsonBody, okhttp3.MediaType.get("application/json")))
                .build();

            StringBuilder fullResponse = new StringBuilder();

            try (okhttp3.Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errorMsg = "请求失败: " + response.code();
                    if (response.code() == 429) {
                        errorMsg = "访问过于频繁 (429)，请稍后再试或检查 API 额度。";
                    } else if (response.code() == 401) {
                        errorMsg = "API 密钥无效或已过期 (401)。";
                    } else if (response.code() == 404) {
                        errorMsg = "未找到该模型或模型不支持当前参数 (404)。";
                    }
                    
                    try {
                        String body = response.body() != null ? response.body().string() : "";
                        log.error("AI 响应错误 ({}): {}", response.code(), body);
                    } catch (Exception ignored) {}
                    
                    throw new RuntimeException(errorMsg);
                }

                okhttp3.ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    try (java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(responseBody.byteStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                if (!"[DONE]".equals(data) && !data.isEmpty()) {
                                    emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(data));

                                    JsonNode node = objectMapper.readTree(data);
                                    JsonNode choices = node.get("choices");
                                    if (choices != null && choices.size() > 0) {
                                        JsonNode delta = choices.get(0).get("delta");
                                        if (delta != null && delta.has("content")) {
                                            fullResponse.append(delta.get("content").asText());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            updateConversationContext(request.getConversationId(), request.getMessage(), fullResponse.toString());

            emitter.send(SseEmitter.event()
                .name("done")
                .data("{\"status\":\"completed\"}"));
            emitter.complete();

        } catch (Exception e) {
            log.error("OpenAI流式请求失败", e);
            try {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"error\":\"OpenAI请求失败: " + e.getMessage() + "\"}"));
                emitter.completeWithError(e);
            } catch (Exception ex) {
                log.error("发送错误消息失败", ex);
            }
        }
    }
}
