package com.example.springboottest.modules.ai.agent.model;

import com.example.springboottest.config.AiProperties;
import com.example.springboottest.entity.DTO.AiChatRequest;
import com.example.springboottest.modules.ai.dto.ConversationVO;
import com.example.springboottest.modules.ai.entity.AiModel;
import com.example.springboottest.modules.ai.service.ChatHistoryService;
import com.example.springboottest.modules.ai.setvice.AiModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DefaultAgentModelRegistry implements AgentModelRegistry {

    private final AiProperties aiProperties;
    private final AiModelService aiModelService;
    private final ChatHistoryService chatHistoryService;

    @Value("${spring.ai.openai.base-url:}")
    private String springAiBaseUrl;

    @Override
    public ResolvedAgentModel resolve(AiChatRequest request) {
        AiModel selectedModel = findSelectedModel(request);
        AgentGenerateOptions options = buildOptions(request);

        if (selectedModel != null) {
            return resolveManagedModel(selectedModel, options);
        }

        String fallbackApiKey = trimToNull(aiProperties.getOpenai().getApiKey());
        String fallbackBaseUrl = resolveFallbackBaseUrl();
        String fallbackModelName = aiModelService.resolveFallbackModelName(options.getUseDeepThinking(), false);

        if (!StringUtils.hasText(fallbackApiKey) || !StringUtils.hasText(fallbackBaseUrl) || !StringUtils.hasText(fallbackModelName)) {
            return ResolvedAgentModel.unavailable("No available model was found. Please enable a default model or complete configuration.");
        }

        ChatModel chatModel = aiModelService.createChatModel(
                fallbackBaseUrl,
                fallbackApiKey,
                fallbackModelName,
                options.getTemperature(),
                options.getMaxTokens(),
                options.getUseDeepThinking()
        );

        return ResolvedAgentModel.builder()
                .available(true)
                .managed(false)
                .chatModel(chatModel)
                .provider("openai")
                .modelName(fallbackModelName)
                .displayName(fallbackModelName)
                .baseUrl(fallbackBaseUrl)
                .apiKey(fallbackApiKey)
                .options(options)
                .build();
    }

    @Override
    public boolean canResolve(AiChatRequest request) {
        return resolve(request).available();
    }

    private ResolvedAgentModel resolveManagedModel(AiModel selectedModel, AgentGenerateOptions options) {
        String apiKey = trimToNull(selectedModel.getApiKey());
        if (!StringUtils.hasText(apiKey)) {
            apiKey = aiModelService.fallbackApiKey();
        }
        if (!StringUtils.hasText(selectedModel.getBaseUrl()) || !StringUtils.hasText(apiKey)) {
            return ResolvedAgentModel.unavailable("The selected model configuration is incomplete. Please check Base URL and API Key.");
        }

        boolean deepThinking = Boolean.TRUE.equals(options.getUseDeepThinking())
                && Boolean.TRUE.equals(selectedModel.getSupportsDeepThinking());
        options.setUseDeepThinking(deepThinking);

        ChatModel chatModel = aiModelService.createChatModel(
                selectedModel.getBaseUrl(),
                apiKey,
                selectedModel.getModelName(),
                options.getTemperature(),
                options.getMaxTokens(),
                deepThinking
        );

        return ResolvedAgentModel.builder()
                .available(true)
                .managed(true)
                .chatModel(chatModel)
                .provider(defaultIfBlank(selectedModel.getProvider(), "openai"))
                .modelName(selectedModel.getModelName())
                .displayName(defaultIfBlank(selectedModel.getDisplayName(), selectedModel.getModelName()))
                .modelId(selectedModel.getId())
                .baseUrl(selectedModel.getBaseUrl())
                .apiKey(apiKey)
                .options(options)
                .build();
    }

    private AiModel findSelectedModel(AiChatRequest request) {
        AiModel selectedModel = null;
        if (request != null && request.getModelId() != null) {
            selectedModel = loadEnabledManagedModel(request.getModelId());
        }
        if (selectedModel == null && request != null && StringUtils.hasText(request.getConversationId())) {
            ConversationVO conversation = chatHistoryService.getConversation(request.getConversationId());
            if (conversation != null && conversation.getModelId() != null) {
                selectedModel = loadEnabledManagedModel(conversation.getModelId());
            }
        }
        return selectedModel == null ? aiModelService.findDefaultModel() : selectedModel;
    }

    private AiModel loadEnabledManagedModel(Long modelId) {
        AiModel candidate = aiModelService.findById(modelId);
        return candidate != null && Boolean.TRUE.equals(candidate.getEnabled()) ? candidate : null;
    }

    private AgentGenerateOptions buildOptions(AiChatRequest request) {
        return AgentGenerateOptions.builder()
                .temperature(request != null && request.getTemperature() != null
                        ? request.getTemperature()
                        : aiProperties.getOpenai().getTemperature())
                .maxTokens(request != null && request.getMaxTokens() != null
                        ? request.getMaxTokens()
                        : aiProperties.getOpenai().getMaxTokens())
                .useDeepThinking(request != null && Boolean.TRUE.equals(request.getUseDeepThinking()))
                .build();
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

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
