package com.example.springboottest.modules.ai.service.impl;

import com.example.springboottest.config.AiProperties;
import com.example.springboottest.entity.DTO.AiChatRequest;
import com.example.springboottest.entity.DTO.AiChatResponse;
import com.example.springboottest.entity.DTO.AiProviderInfo;
import com.example.springboottest.modules.ai.dto.AiModelResponse;
import com.example.springboottest.modules.ai.dto.ConversationVO;
import com.example.springboottest.modules.ai.dto.MessageVO;
import com.example.springboottest.modules.ai.dto.SearchStatus;
import com.example.springboottest.modules.ai.dto.WebSearchSource;
import com.example.springboottest.modules.ai.entity.AiModel;
import com.example.springboottest.modules.ai.service.AiAgentService;
import com.example.springboottest.modules.ai.service.AiChatService;
import com.example.springboottest.modules.ai.service.ChatHistoryService;
import com.example.springboottest.modules.ai.setvice.AiModelService;
import com.example.springboottest.modules.ai.skill.entity.AiSkill;
import com.example.springboottest.modules.ai.skill.service.AiSkillService;
import com.example.springboottest.modules.ai.websearch.WebSearchContext;
import com.example.springboottest.modules.ai.websearch.WebSearchService;
import org.springframework.ai.chat.messages.AssistantMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
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
    private final WebSearchService webSearchService;
    private final AiAgentService aiAgentService;
    private final AiSkillService aiSkillService;

    @Value("${spring.ai.openai.base-url:}")
    private String springAiBaseUrl;

    @Override
    public CompletableFuture<AiChatResponse> chat(AiChatRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            log.info("Handling AI chat request: {}", request.getMessage());
            if (Boolean.TRUE.equals(request.getUseAgent())) {
                return chatWithAgent(request, startTime);
            }
            return chatWithSpringAi(request, startTime);
        });
    }

    @Override
    public void chatStream(AiChatRequest request, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            try {
                if (Boolean.TRUE.equals(request.getUseAgent())) {
                    chatStreamWithAgent(request, emitter);
                    return;
                }
                chatStreamWithSpringAi(request, emitter);
            } catch (Exception e) {
                log.error("Streaming AI chat request failed", e);
                sendEmitterError(emitter, e.getMessage(), e);
            }
        });
    }

    private AiChatResponse chatWithAgent(AiChatRequest request, long startTime) {
        ResolvedChatModel resolved = resolveChatModel(request);
        if (!resolved.available()) {
            return aiModelService.buildFallbackErrorResponse(startTime, resolved.errorMessage());
        }

        try {
            bindConversationModel(request, resolved);
            syncRequestMountedSkills(request);
            saveUserMessage(request, resolved, disabledSearchContext());
            List<Message> historyMessages = buildAgentHistoryMessages(request.getConversationId(), request.getMessage());
            List<AiSkill> mountedSkills = aiSkillService.getMountedEnabledSkills(request.getConversationId());
            String mountedSkillPrompt = aiSkillService.buildMountedSkillPrompt(request.getConversationId());
            AiAgentService.AgentRunResult result = aiAgentService.run(
                    toAgentModelConfig(resolved, request),
                    request.getConversationId(),
                    request.getMessage(),
                    historyMessages,
                    mountedSkills,
                    mountedSkillPrompt,
                    event -> {
                    }
            );

            String answer = defaultIfBlank(result.getAnswer(), "抱歉，Agent 暂时没有生成有效回复。");
            updateConversationContext(request.getConversationId(), request.getMessage(), answer);
            saveAssistantMessage(request.getConversationId(), answer, null, null, false, disabledSearchContext());

            return AiChatResponse.builder()
                    .success(result.isSuccess())
                    .message(answer)
                    .error(result.getError())
                    .provider(resolved.provider())
                    .model(resolved.modelName())
                    .conversationId(request.getConversationId())
                    .usedWebSearch(false)
                    .searchStatus(SearchStatus.NOT_REQUESTED)
                    .agentRunId(result.getRunId())
                    .agentStatus(result.getStatus())
                    .responseTime(System.currentTimeMillis() - startTime)
                    .build();
        } catch (Exception e) {
            log.error("Agent chat request failed", e);
            return AiChatResponse.builder()
                    .success(false)
                    .error("Agent request failed: " + e.getMessage())
                    .provider(resolved.provider())
                    .model(resolved.modelName())
                    .usedWebSearch(false)
                    .searchStatus(SearchStatus.NOT_REQUESTED)
                    .responseTime(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private AiChatResponse chatWithSpringAi(AiChatRequest request, long startTime) {
        ResolvedChatModel resolved = resolveChatModel(request);
        if (!resolved.available()) {
            return aiModelService.buildFallbackErrorResponse(startTime, resolved.errorMessage());
        }

        WebSearchContext searchContext = webSearchService.prepareContext(Boolean.TRUE.equals(request.getUseWebSearch()), request.getMessage());
        if (searchContext.abortChat()) {
            return buildSearchFailureResponse(startTime, resolved, searchContext);
        }

        try {
            bindConversationModel(request, resolved);
            syncRequestMountedSkills(request);
            saveUserMessage(request, resolved, searchContext);

            List<Message> messages = buildMessages(request, searchContext);
            Prompt prompt = new Prompt(messages, buildChatOptions(request, resolved));
            ChatResponse response = resolved.chatModel().call(prompt);
            String aiMessage = extractResponseText(response);

            updateConversationContext(request.getConversationId(), request.getMessage(), aiMessage);
            saveAssistantMessage(request.getConversationId(), aiMessage, null, null, resolved.useDeepThinking(), searchContext);

            return parseSpringAiResponse(response, request, resolved, searchContext, startTime);
        } catch (Exception e) {
            log.error("Spring AI request failed", e);
            return AiChatResponse.builder()
                    .success(false)
                    .error("AI request failed: " + e.getMessage())
                    .provider(resolved.provider())
                    .model(resolved.modelName())
                    .usedWebSearch(searchContext.requested())
                    .searchQuery(searchContext.searchQuery())
                    .searchStatus(searchContext.searchStatus())
                    .sources(searchContext.sources())
                    .responseTime(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private void chatStreamWithSpringAi(AiChatRequest request, SseEmitter emitter) {
        ResolvedChatModel resolved = resolveChatModel(request);
        if (!resolved.available()) {
            sendEmitterError(emitter, resolved.errorMessage(), null);
            emitter.complete();
            return;
        }

        WebSearchContext searchContext = webSearchService.prepareContext(Boolean.TRUE.equals(request.getUseWebSearch()), request.getMessage());
        if (searchContext.abortChat()) {
            sendEmitterError(emitter, defaultIfBlank(searchContext.errorMessage(), "Web search failed"), null);
            emitter.complete();
            return;
        }

        try {
            bindConversationModel(request, resolved);
            syncRequestMountedSkills(request);
            saveUserMessage(request, resolved, searchContext);
            sendSearchEvent(emitter, searchContext);

            List<Message> messages = buildMessages(request, searchContext);
            Prompt prompt = new Prompt(messages, buildChatOptions(request, resolved));
            Flux<ChatResponse> flux = resolved.chatModel().stream(prompt);

            StringBuilder fullResponse = new StringBuilder();

            flux.subscribe(
                    chatResponse -> {
                        try {
                            String content = extractResponseText(chatResponse);
                            if (StringUtils.hasText(content)) {
                                fullResponse.append(content);
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(convertToSseFormat(chatResponse)));
                            }
                        } catch (Exception e) {
                            log.error("Failed to send streaming token", e);
                        }
                    },
                    error -> {
                        log.error("Streaming chat call failed", error);
                        sendEmitterError(emitter, error.getMessage(), error);
                    },
                    () -> {
                        String assistantMessage = fullResponse.toString();
                        try {
                            updateConversationContext(request.getConversationId(), request.getMessage(), assistantMessage);
                            saveAssistantMessage(request.getConversationId(), assistantMessage, null, null, resolved.useDeepThinking(), searchContext);
                            emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data("{\"status\":\"completed\"}"));
                        } catch (Exception e) {
                            log.error("Failed to finish streaming response", e);
                            sendEmitterError(emitter, e.getMessage(), e);
                            return;
                        }

                        emitter.complete();
                    }
            );
        } catch (Exception e) {
            log.error("Spring AI streaming request failed", e);
            sendEmitterError(emitter, "AI request failed: " + e.getMessage(), e);
        }
    }

    private void chatStreamWithAgent(AiChatRequest request, SseEmitter emitter) {
        ResolvedChatModel resolved = resolveChatModel(request);
        if (!resolved.available()) {
            sendEmitterError(emitter, resolved.errorMessage(), null);
            emitter.complete();
            return;
        }

        try {
            bindConversationModel(request, resolved);
            syncRequestMountedSkills(request);
            saveUserMessage(request, resolved, disabledSearchContext());
            List<Message> historyMessages = buildAgentHistoryMessages(request.getConversationId(), request.getMessage());
            List<AiSkill> mountedSkills = aiSkillService.getMountedEnabledSkills(request.getConversationId());
            String mountedSkillPrompt = aiSkillService.buildMountedSkillPrompt(request.getConversationId());

            AiAgentService.AgentRunResult result = aiAgentService.run(
                    toAgentModelConfig(resolved, request),
                    request.getConversationId(),
                    request.getMessage(),
                    historyMessages,
                    mountedSkills,
                    mountedSkillPrompt,
                    event -> sendAgentEvent(emitter, event)
            );

            String answer = defaultIfBlank(result.getAnswer(), "抱歉，Agent 暂时没有生成有效回复。");
            updateConversationContext(request.getConversationId(), request.getMessage(), answer);
            saveAssistantMessage(request.getConversationId(), answer, null, null, false, disabledSearchContext());

            emitAssistantMessage(emitter, new AssistantMessage(answer));
            emitter.send(SseEmitter.event()
                    .name("done")
                    .data(objectMapper.writeValueAsString(Map.of(
                            "status", defaultIfBlank(result.getStatus(), "completed"),
                            "agentRunId", defaultIfBlank(result.getRunId(), "")
                    ))));

            emitter.complete();
        } catch (Exception e) {
            log.error("Agent streaming request failed", e);
            sendEmitterError(emitter, "Agent request failed: " + e.getMessage(), e);
        }
    }

    private void bindConversationModel(AiChatRequest request, ResolvedChatModel resolved) {
        if (!StringUtils.hasText(request.getConversationId())) {
            return;
        }
        chatHistoryService.bindConversationModel(
                request.getConversationId(),
                resolved.modelId(),
                resolved.provider(),
                resolved.modelName(),
                resolved.displayName()
        );
    }

    private void saveUserMessage(AiChatRequest request, ResolvedChatModel resolved, WebSearchContext searchContext) {
        try {
            chatHistoryService.saveUserMessage(
                    request.getConversationId(),
                    request.getMessage(),
                    searchContext.requested(),
                    resolved.useDeepThinking()
            );
        } catch (Exception e) {
            log.warn("Failed to save user message", e);
        }
    }

    private void saveAssistantMessage(String conversationId,
                                      String content,
                                      String thought,
                                      Integer thinkingTime,
                                      boolean usedDeepThinking,
                                      WebSearchContext searchContext) {
        try {
            chatHistoryService.saveAssistantMessage(
                    conversationId,
                    content,
                    thought,
                    thinkingTime,
                    searchContext.requested(),
                    usedDeepThinking,
                    searchContext.searchQuery(),
                    defaultSearchStatus(searchContext.searchStatus()),
                    searchContext.sources()
            );
        } catch (Exception e) {
            log.warn("Failed to save assistant message", e);
        }
    }

    private void sendSearchEvent(SseEmitter emitter, WebSearchContext searchContext) {
        if (!searchContext.requested()) {
            return;
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("usedWebSearch", true);
            payload.put("searchQuery", searchContext.searchQuery());
            payload.put("searchStatus", defaultSearchStatus(searchContext.searchStatus()));
            payload.put("sources", searchContext.sources());
            emitter.send(SseEmitter.event()
                    .name("search")
                    .data(objectMapper.writeValueAsString(payload)));
        } catch (Exception e) {
            log.warn("Failed to send search SSE event", e);
        }
    }

    private WebSearchContext disabledSearchContext() {
        return WebSearchContext.builder()
                .requested(false)
                .success(false)
                .abortChat(false)
                .searchStatus(SearchStatus.NOT_REQUESTED)
                .sources(Collections.emptyList())
                .build();
    }

    private void sendAgentEvent(SseEmitter emitter, AiAgentService.AgentEvent event) {
        if (event == null || !StringUtils.hasText(event.getType())) {
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name(event.getType())
                    .data(objectMapper.writeValueAsString(event.getPayload())));
        } catch (Exception e) {
            log.warn("Failed to send agent SSE event: {}", event.getType(), e);
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
            String selectedModelApiKey = trimToNull(selectedModel.getApiKey());
            if (!StringUtils.hasText(selectedModelApiKey)) {
                selectedModelApiKey = aiModelService.fallbackApiKey();
            }
            if (!StringUtils.hasText(selectedModel.getBaseUrl()) || !StringUtils.hasText(selectedModelApiKey)) {
                return ResolvedChatModel.unavailable("The selected model configuration is incomplete. Please check Base URL and API Key.");
            }
            boolean useDeepThinking = shouldEnableDeepThinking(
                    request.getUseAgent(),
                    request.getUseDeepThinking(),
                    selectedModel.getSupportsDeepThinking()
            );
            ChatModel chatModel = aiModelService.createChatModel(
                    selectedModel.getBaseUrl(),
                    selectedModelApiKey,
                    selectedModel.getModelName(),
                    request.getTemperature() != null ? request.getTemperature() : aiProperties.getOpenai().getTemperature(),
                    request.getMaxTokens() != null ? request.getMaxTokens() : aiProperties.getOpenai().getMaxTokens(),
                    useDeepThinking
            );
            Double temperature = request.getTemperature() != null ? request.getTemperature() : aiProperties.getOpenai().getTemperature();
            Integer maxTokens = request.getMaxTokens() != null ? request.getMaxTokens() : aiProperties.getOpenai().getMaxTokens();
            return ResolvedChatModel.builder()
                    .available(true)
                    .managed(true)
                    .chatModel(chatModel)
                    .provider(defaultIfBlank(selectedModel.getProvider(), "openai"))
                    .modelName(selectedModel.getModelName())
                    .displayName(defaultIfBlank(selectedModel.getDisplayName(), selectedModel.getModelName()))
                    .modelId(selectedModel.getId())
                    .baseUrl(selectedModel.getBaseUrl())
                    .apiKey(selectedModelApiKey)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .useDeepThinking(useDeepThinking)
                    .useWebSearch(Boolean.TRUE.equals(request.getUseWebSearch()))
                    .build();
        }

        String fallbackApiKey = trimToNull(aiProperties.getOpenai().getApiKey());
        String fallbackBaseUrl = resolveFallbackBaseUrl();
        String fallbackModelName = aiModelService.resolveFallbackModelName(request.getUseDeepThinking(), false);

        if (!StringUtils.hasText(fallbackApiKey) || !StringUtils.hasText(fallbackBaseUrl) || !StringUtils.hasText(fallbackModelName)) {
            return ResolvedChatModel.unavailable("No available model was found. Please enable a default model or complete configuration.");
        }

        boolean useDeepThinking = shouldEnableDeepThinking(
                request.getUseAgent(),
                request.getUseDeepThinking(),
                !Boolean.TRUE.equals(request.getUseAgent()) && StringUtils.hasText(aiProperties.getOpenai().getThinkingModel())
        );

        ChatModel fallbackChatModel = aiModelService.createChatModel(
                fallbackBaseUrl,
                fallbackApiKey,
                fallbackModelName,
                request.getTemperature() != null ? request.getTemperature() : aiProperties.getOpenai().getTemperature(),
                request.getMaxTokens() != null ? request.getMaxTokens() : aiProperties.getOpenai().getMaxTokens(),
                useDeepThinking
        );
        Double temperature = request.getTemperature() != null ? request.getTemperature() : aiProperties.getOpenai().getTemperature();
        Integer maxTokens = request.getMaxTokens() != null ? request.getMaxTokens() : aiProperties.getOpenai().getMaxTokens();

        return ResolvedChatModel.builder()
                .available(true)
                .managed(false)
                .chatModel(fallbackChatModel)
                .provider("openai")
                .modelName(fallbackModelName)
                .displayName(fallbackModelName)
                .modelId(null)
                .baseUrl(fallbackBaseUrl)
                .apiKey(fallbackApiKey)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .useDeepThinking(useDeepThinking)
                .useWebSearch(Boolean.TRUE.equals(request.getUseWebSearch()))
                .build();
    }

    private void syncRequestMountedSkills(AiChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getConversationId()) || request.getSkillIds() == null) {
            return;
        }
        aiSkillService.saveConversationSkills(request.getConversationId(), request.getSkillIds());
    }

    static boolean shouldEnableDeepThinking(Boolean useAgent,
                                            Boolean useDeepThinking,
                                            Boolean modelSupportsDeepThinking) {
        return !Boolean.TRUE.equals(useAgent)
                && Boolean.TRUE.equals(useDeepThinking)
                && Boolean.TRUE.equals(modelSupportsDeepThinking);
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

    private List<Message> buildMessages(AiChatRequest request, WebSearchContext searchContext) {
        List<Message> messages = new ArrayList<>();
        if (StringUtils.hasText(request.getConversationId())) {
            String context = conversationContexts.get(request.getConversationId());
            if (StringUtils.hasText(context)) {
                messages.add(new SystemMessage(context));
            }
        }
        if (searchContext.success() && StringUtils.hasText(searchContext.promptContext())) {
            messages.add(new SystemMessage("""
                    You may receive web search context for the user's question.
                    For latest or factual claims, prioritize that web search context over unstated prior knowledge.
                    If the search context is insufficient or unrelated, say so clearly before answering cautiously.
                    """));
        }
        String mountedSkillPrompt = aiSkillService.buildMountedSkillPrompt(request.getConversationId());
        if (StringUtils.hasText(mountedSkillPrompt)) {
            messages.add(new SystemMessage(mountedSkillPrompt));
        }
        messages.add(new UserMessage(buildUserMessage(request.getMessage(), searchContext)));
        return messages;
    }

    private AiAgentService.AgentModelConfig toAgentModelConfig(ResolvedChatModel resolved, AiChatRequest request) {
        return new AiAgentService.AgentModelConfig(
                resolved.provider(),
                resolved.baseUrl(),
                resolved.apiKey(),
                resolved.modelName(),
                resolved.temperature(),
                resolved.maxTokens(),
                request == null ? null : request.getAgentMode(),
                request == null ? null : request.getPermissionMode(),
                request == null ? null : request.getResumeRunId()
        );
    }

    private List<Message> buildAgentHistoryMessages(String conversationId, String latestUserMessage) {
        if (!StringUtils.hasText(conversationId)) {
            return Collections.emptyList();
        }
        List<MessageVO> history = chatHistoryService.getConversationMessages(conversationId);
        if (history == null || history.isEmpty()) {
            return Collections.emptyList();
        }

        int endExclusive = history.size();
        MessageVO latest = history.get(history.size() - 1);
        if ("user".equalsIgnoreCase(latest.getRole())
                && StringUtils.hasText(latestUserMessage)
                && latestUserMessage.trim().equals(defaultIfBlank(latest.getContent(), ""))) {
            endExclusive = history.size() - 1;
        }
        if (endExclusive <= 0) {
            return Collections.emptyList();
        }

        int fromIndex = Math.max(0, endExclusive - 12);
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("""
                以下是当前会话的历史消息，请结合上下文继续完成本轮任务。
                如果历史内容与本轮问题无关，请以本轮问题为主。
                """));

        for (MessageVO item : history.subList(fromIndex, endExclusive)) {
            if ("assistant".equalsIgnoreCase(item.getRole())) {
                messages.add(new AssistantMessage(defaultIfBlank(item.getContent(), "")));
            } else {
                messages.add(new UserMessage(defaultIfBlank(item.getContent(), "")));
            }
        }
        return messages;
    }

    static String buildUserMessage(String userMessage, WebSearchContext searchContext) {
        if (searchContext == null || !searchContext.success() || !StringUtils.hasText(searchContext.promptContext())) {
            return userMessage;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("User question:\n");
        builder.append(defaultIfBlank(userMessage, "")).append("\n\n");
        builder.append("Use the following web search context to answer the question.\n");
        builder.append("When using these sources, mention the source title or domain when helpful.\n");
        builder.append("If the context does not actually answer the question, say that clearly.\n\n");
        builder.append(searchContext.promptContext());
        return builder.toString();
    }

    private OpenAiChatOptions buildChatOptions(AiChatRequest request, ResolvedChatModel resolved) {
        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder().model(resolved.modelName());
        if (request.getMaxTokens() != null) {
            builder.maxTokens(request.getMaxTokens());
        }
        if (request.getTemperature() != null) {
            builder.temperature(request.getTemperature());
        }
        return builder.build();
    }

    private AiChatResponse parseSpringAiResponse(ChatResponse response,
                                                 AiChatRequest request,
                                                 ResolvedChatModel resolved,
                                                 WebSearchContext searchContext,
                                                 long startTime) {
        try {
            Integer tokensUsed = null;
            if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                tokensUsed = (int) response.getMetadata().getUsage().getTotalTokens();
            }

            return AiChatResponse.builder()
                    .message(extractResponseText(response))
                    .provider(resolved.provider())
                    .model(resolved.modelName())
                    .conversationId(request.getConversationId())
                    .tokensUsed(tokensUsed)
                    .usedWebSearch(searchContext.requested())
                    .searchQuery(searchContext.searchQuery())
                    .searchStatus(defaultSearchStatus(searchContext.searchStatus()))
                    .sources(searchContext.sources())
                    .responseTime(System.currentTimeMillis() - startTime)
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse Spring AI response", e);
            return AiChatResponse.builder()
                    .success(false)
                    .error("Failed to parse response: " + e.getMessage())
                    .provider(resolved.provider())
                    .model(resolved.modelName())
                    .usedWebSearch(searchContext.requested())
                    .searchQuery(searchContext.searchQuery())
                    .searchStatus(defaultSearchStatus(searchContext.searchStatus()))
                    .sources(searchContext.sources())
                    .responseTime(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private AiChatResponse buildSearchFailureResponse(long startTime, ResolvedChatModel resolved, WebSearchContext searchContext) {
        return AiChatResponse.builder()
                .success(false)
                .error(defaultIfBlank(searchContext.errorMessage(), "Web search failed"))
                .provider(resolved.provider())
                .model(resolved.modelName())
                .usedWebSearch(true)
                .searchQuery(searchContext.searchQuery())
                .searchStatus(defaultSearchStatus(searchContext.searchStatus()))
                .sources(searchContext.sources())
                .responseTime(System.currentTimeMillis() - startTime)
                .build();
    }

    private String extractResponseText(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        return response.getResult().getOutput().getText();
    }

    private String convertToSseFormat(ChatResponse chatResponse) {
        try {
            Map<String, Object> delta = new LinkedHashMap<>();
            String content = extractResponseText(chatResponse);
            if (content != null) {
                delta.put("content", content);
            }
            Map<String, Object> choice = new LinkedHashMap<>();
            choice.put("delta", delta);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("choices", List.of(choice));
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Failed to convert SSE payload", e);
            return "{\"choices\":[{\"delta\":{\"content\":\"\"}}]}";
        }
    }

    private void emitAssistantMessage(SseEmitter emitter, AssistantMessage assistantMessage) {
        try {
            Map<String, Object> delta = new LinkedHashMap<>();
            delta.put("content", assistantMessage == null ? "" : defaultIfBlank(assistantMessage.getText(), ""));
            Map<String, Object> choice = new LinkedHashMap<>();
            choice.put("delta", delta);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("choices", List.of(choice));
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(payload)));
        } catch (Exception e) {
            log.warn("Failed to emit final assistant message for agent", e);
        }
    }

    private void updateConversationContext(String conversationId, String userMessage, String aiMessage) {
        if (!StringUtils.hasText(conversationId)) {
            return;
        }
        String context = conversationContexts.getOrDefault(conversationId, "");
        context += "User: " + userMessage + "\nAI: " + aiMessage + "\n";
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
            log.error("Failed to send SSE error", ex);
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

    private SearchStatus defaultSearchStatus(SearchStatus status) {
        return status == null ? SearchStatus.NOT_REQUESTED : status;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    @Builder
    private record ResolvedChatModel(
            boolean available,
            boolean managed,
            ChatModel chatModel,
            String provider,
            String modelName,
            String displayName,
            Long modelId,
            String baseUrl,
            String apiKey,
            Double temperature,
            Integer maxTokens,
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
