package com.example.springboottest.modules.ai.service.impl;

import com.example.springboottest.config.AiProperties;
import com.example.springboottest.entity.DTO.AiChatRequest;
import com.example.springboottest.entity.DTO.AiChatResponse;
import com.example.springboottest.entity.DTO.AiProviderInfo;
import com.example.springboottest.modules.ai.dto.AiModelResponse;
import com.example.springboottest.modules.ai.dto.ConversationVO;
import com.example.springboottest.modules.ai.entity.AiModel;
import com.example.springboottest.modules.ai.service.AiChatService;
import com.example.springboottest.modules.ai.service.ChatHistoryService;
import com.example.springboottest.modules.ai.setvice.AiModelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiProperties aiProperties;
    private final ChatHistoryService chatHistoryService;
    private final ObjectMapper objectMapper;
    private final AiModelService aiModelService;
    private final Map<String, String> conversationContexts;

    @Value("${spring.ai.openai.base-url:}")
    private String springAiBaseUrl;

    @Override
    public CompletableFuture<AiChatResponse> chat(AiChatRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            log.info("处理AI聊天请求: {}", request.getMessage());
            return chatWithSpringAi(request, startTime);
        });
    }

    @Override
    public void chatStream(AiChatRequest request, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            try {
                chatStreamWithSpringAi(request, emitter);
            } catch (Exception e) {
                log.error("流式聊天请求处理失败", e);
                sendEmitterError(emitter, e.getMessage(), e);
            }
        });
    }

    private AiChatResponse chatWithSpringAi(AiChatRequest request, long startTime) {
        try {
            ResolvedChatModel resolved = resolveChatModel(request);
            if (!resolved.available()) {
                return aiModelService.buildFallbackErrorResponse(startTime, resolved.errorMessage());
            }

            List<Message> messages = buildMessages(request);
            Prompt prompt = new Prompt(messages, buildChatOptions(request, resolved));
            ChatResponse response = resolved.chatModel().call(prompt);
            updateConversationContext(request.getConversationId(), request.getMessage(), response.getResult().getOutput().getText());
            return parseSpringAiResponse(response, request, resolved, startTime);
        } catch (Exception e) {
            log.error("Spring AI请求失败", e);
            return AiChatResponse.builder()
                    .success(false)
                    .error("AI请求失败: " + e.getMessage())
                    .provider("openai")
                    .responseTime(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private void chatStreamWithSpringAi(AiChatRequest request, SseEmitter emitter) {
        try {
            ResolvedChatModel resolved = resolveChatModel(request);
            if (!resolved.available()) {
                sendEmitterError(emitter, resolved.errorMessage(), null);
                emitter.complete();
                return;
            }

            if (StringUtils.hasText(request.getConversationId())) {
                chatHistoryService.bindConversationModel(
                        request.getConversationId(),
                        resolved.modelId(),
                        resolved.provider(),
                        resolved.modelName(),
                        resolved.displayName()
                );
            }

            try {
                chatHistoryService.saveUserMessage(
                        request.getConversationId(),
                        request.getMessage(),
                        resolved.useWebSearch(),
                        resolved.useDeepThinking()
                );
            } catch (Exception e) {
                log.warn("保存用户消息失败", e);
            }

            List<Message> messages = buildMessages(request);
            Prompt prompt = new Prompt(messages, buildChatOptions(request, resolved));
            Flux<ChatResponse> flux = resolved.chatModel().stream(prompt);

            StringBuilder fullResponse = new StringBuilder();

            flux.subscribe(
                    chatResponse -> {
                        try {
                            String content = chatResponse.getResult().getOutput().getText();
                            if (StringUtils.hasText(content)) {
                                fullResponse.append(content);
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(convertToSseFormat(chatResponse)));
                            }
                        } catch (Exception e) {
                            log.error("发送流式消息失败", e);
                        }
                    },
                    error -> {
                        log.error("流式调用失败", error);
                        sendEmitterError(emitter, error.getMessage(), error);
                    },
                    () -> {
                        try {
                            updateConversationContext(request.getConversationId(), request.getMessage(), fullResponse.toString());
                            try {
                                chatHistoryService.saveAssistantMessage(
                                        request.getConversationId(),
                                        fullResponse.toString(),
                                        null,
                                        null
                                );
                            } catch (Exception e) {
                                log.warn("保存AI回复失败", e);
                            }

                            emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data("{\"status\":\"completed\"}"));
                            emitter.complete();
                        } catch (Exception e) {
                            log.error("完成流式响应失败", e);
                        }
                    }
            );
        } catch (Exception e) {
            log.error("Spring AI流式请求失败", e);
            sendEmitterError(emitter, "AI请求失败: " + e.getMessage(), e);
        }
    }

    private ResolvedChatModel resolveChatModel(AiChatRequest request) {
        AiModel selectedModel = null;

        if (request.getModelId() != null) {
            selectedModel = loadEnabledManagedModel(request.getModelId());
        }

        if (selectedModel == null && StringUtils.hasText(request.getConversationId())) {
            ConversationVO conversation = chatHistoryService.getConversation(request.getConversationId());
            if (conversation != null && conversation.getModelId() != null) {
                selectedModel = loadEnabledManagedModel(conversation.getModelId());
            }
        }

        if (selectedModel == null) {
            selectedModel = aiModelService.findDefaultModel();
        }

        if (selectedModel != null) {
            if (!StringUtils.hasText(selectedModel.getBaseUrl()) || !StringUtils.hasText(selectedModel.getApiKey())) {
                return ResolvedChatModel.unavailable("所选模型配置不完整，请检查 Base URL 和 API Key");
            }
            boolean useDeepThinking = Boolean.TRUE.equals(request.getUseDeepThinking())
                    && Boolean.TRUE.equals(selectedModel.getSupportsDeepThinking());
            boolean useWebSearch = Boolean.TRUE.equals(request.getUseWebSearch())
                    && Boolean.TRUE.equals(selectedModel.getSupportsWebSearch());
            OpenAiChatModel chatModel = aiModelService.createChatModel(
                    selectedModel.getBaseUrl(),
                    selectedModel.getApiKey(),
                    selectedModel.getModelName(),
                    request.getTemperature() != null ? request.getTemperature() : aiProperties.getOpenai().getTemperature(),
                    request.getMaxTokens() != null ? request.getMaxTokens() : aiProperties.getOpenai().getMaxTokens()
            );
            return ResolvedChatModel.builder()
                    .available(true)
                    .managed(true)
                    .chatModel(chatModel)
                    .provider(defaultIfBlank(selectedModel.getProvider(), "openai"))
                    .modelName(selectedModel.getModelName())
                    .displayName(defaultIfBlank(selectedModel.getDisplayName(), selectedModel.getModelName()))
                    .modelId(selectedModel.getId())
                    .useDeepThinking(useDeepThinking)
                    .useWebSearch(useWebSearch)
                    .build();
        }

        String fallbackApiKey = trimToNull(aiProperties.getOpenai().getApiKey());
        String fallbackBaseUrl = resolveFallbackBaseUrl();
        String fallbackModelName = aiModelService.resolveFallbackModelName(request.getUseDeepThinking(), request.getUseWebSearch());

        if (!StringUtils.hasText(fallbackApiKey) || !StringUtils.hasText(fallbackBaseUrl) || !StringUtils.hasText(fallbackModelName)) {
            return ResolvedChatModel.unavailable("未找到可用模型，请先在基座模型管理中启用默认模型或补全配置文件");
        }

        OpenAiChatModel fallbackChatModel = aiModelService.createChatModel(
                fallbackBaseUrl,
                fallbackApiKey,
                fallbackModelName,
                request.getTemperature() != null ? request.getTemperature() : aiProperties.getOpenai().getTemperature(),
                request.getMaxTokens() != null ? request.getMaxTokens() : aiProperties.getOpenai().getMaxTokens()
        );

        return ResolvedChatModel.builder()
                .available(true)
                .managed(false)
                .chatModel(fallbackChatModel)
                .provider("openai")
                .modelName(fallbackModelName)
                .displayName(fallbackModelName)
                .modelId(null)
                .useDeepThinking(Boolean.TRUE.equals(request.getUseDeepThinking()))
                .useWebSearch(Boolean.TRUE.equals(request.getUseWebSearch()))
                .build();
    }

    private AiModel loadEnabledManagedModel(Long modelId) {
        AiModel candidate = aiModelService.findById(modelId);
        return candidate != null && Boolean.TRUE.equals(candidate.getEnabled()) ? candidate : null;
    }

    private String resolveFallbackBaseUrl() {
        if (StringUtils.hasText(springAiBaseUrl)) {
            return springAiBaseUrl.trim();
        }
        String apiUrl = trimToNull(aiProperties.getOpenai().getApiUrl());
        if (!StringUtils.hasText(apiUrl)) {
            return null;
        }
        String normalized = apiUrl.trim();
        normalized = normalized.replaceAll("/chat/completions/?$", "");
        return normalized.replaceAll("/v1/?chat/completions/?$", "/v1");
    }

    private List<Message> buildMessages(AiChatRequest request) {
        List<Message> messages = new ArrayList<>();
        if (StringUtils.hasText(request.getConversationId())) {
            String context = conversationContexts.get(request.getConversationId());
            if (StringUtils.hasText(context)) {
                messages.add(new SystemMessage(context));
            }
        }
        messages.add(new UserMessage(request.getMessage()));
        return messages;
    }

    private OpenAiChatOptions buildChatOptions(AiChatRequest request, ResolvedChatModel resolved) {
        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder()
                .model(resolved.modelName());

        if (request.getMaxTokens() != null) {
            builder.maxTokens(request.getMaxTokens());
        }
        if (request.getTemperature() != null) {
            builder.temperature(request.getTemperature());
        }
        return builder.build();
    }

    private AiChatResponse parseSpringAiResponse(ChatResponse response, AiChatRequest request, ResolvedChatModel resolved, long startTime) {
        try {
            String message = response.getResult().getOutput().getText();
            Integer tokensUsed = null;
            if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                tokensUsed = (int) response.getMetadata().getUsage().getTotalTokens();
            }

            return AiChatResponse.builder()
                    .message(message)
                    .provider(resolved.provider())
                    .model(resolved.modelName())
                    .conversationId(request.getConversationId())
                    .tokensUsed(tokensUsed)
                    .responseTime(System.currentTimeMillis() - startTime)
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.error("解析Spring AI响应失败", e);
            return AiChatResponse.builder()
                    .success(false)
                    .error("解析响应失败: " + e.getMessage())
                    .provider(resolved.provider())
                    .model(resolved.modelName())
                    .responseTime(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String convertToSseFormat(ChatResponse chatResponse) {
        try {
            Map<String, Object> delta = new LinkedHashMap<>();
            String content = chatResponse.getResult().getOutput().getText();
            if (content != null) {
                delta.put("content", content);
            }
            Map<String, Object> choice = new LinkedHashMap<>();
            choice.put("delta", delta);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("choices", List.of(choice));
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("转换SSE格式失败", e);
            return "{\"choices\":[{\"delta\":{\"content\":\"\"}}]}";
        }
    }

    private void updateConversationContext(String conversationId, String userMessage, String aiMessage) {
        if (!StringUtils.hasText(conversationId)) {
            return;
        }
        String context = conversationContexts.getOrDefault(conversationId, "");
        context += "用户: " + userMessage + "\nAI: " + aiMessage + "\n";
        conversationContexts.put(conversationId, context);
    }

    private void sendEmitterError(SseEmitter emitter, String message, Throwable throwable) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"error\":\"" + escapeJson(message) + "\"}"));
            if (throwable != null) {
                emitter.completeWithError(throwable);
            }
        } catch (Exception ex) {
            log.error("发送错误消息失败", ex);
        }
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public List<AiProviderInfo> getAvailableProviders() {
        List<AiModelResponse> managedModels = aiModelService.listEnabledModels();
        if (managedModels.isEmpty()) {
            return Collections.singletonList(AiProviderInfo.builder()
                    .provider("openai")
                    .displayName("OpenAI Compatible")
                    .availableModels(Collections.singletonList(aiProperties.getOpenai().getModel()))
                    .available(StringUtils.hasText(aiProperties.getOpenai().getApiKey()))
                    .description("Configuration-file fallback model")
                    .build());
        }

        Map<String, List<AiModelResponse>> grouped = managedModels.stream()
                .collect(Collectors.groupingBy(model -> defaultIfBlank(model.getProvider(), "openai")));

        List<AiProviderInfo> providers = new ArrayList<>();
        for (Map.Entry<String, List<AiModelResponse>> entry : grouped.entrySet()) {
            providers.add(AiProviderInfo.builder()
                    .provider(entry.getKey())
                    .displayName(entry.getKey())
                    .availableModels(entry.getValue().stream().map(AiModelResponse::getModelName).toList())
                    .available(true)
                    .description("Managed OpenAI-compatible models")
                    .build());
        }
        return providers;
    }

    @Override
    public boolean isProviderAvailable(String provider) {
        return getAvailableProviders().stream()
                .anyMatch(item -> item.getProvider().equalsIgnoreCase(provider) && item.isAvailable());
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    @Builder
    private record ResolvedChatModel(
            boolean available,
            boolean managed,
            OpenAiChatModel chatModel,
            String provider,
            String modelName,
            String displayName,
            Long modelId,
            boolean useDeepThinking,
            boolean useWebSearch,
            String errorMessage
    ) {
        private static ResolvedChatModel unavailable(String errorMessage) {
            return ResolvedChatModel.builder()
                    .available(false)
                    .errorMessage(errorMessage)
                    .build();
        }
    }
}
