package com.example.springboottest.modules.ai.service.impl;

import com.example.springboottest.config.AiProperties;
import com.example.springboottest.entity.DTO.AiChatRequest;
import com.example.springboottest.entity.DTO.AiChatResponse;
import com.example.springboottest.entity.DTO.AiProviderInfo;
import com.example.springboottest.modules.ai.dto.AiModelResponse;
import com.example.springboottest.modules.ai.dto.ConversationVO;
import com.example.springboottest.modules.ai.dto.SearchStatus;
import com.example.springboottest.modules.ai.dto.WebSearchSource;
import com.example.springboottest.modules.ai.entity.AiModel;
import com.example.springboottest.modules.ai.service.AiChatService;
import com.example.springboottest.modules.ai.service.ChatHistoryService;
import com.example.springboottest.modules.ai.setvice.AiModelService;
import com.example.springboottest.modules.ai.websearch.WebSearchContext;
import com.example.springboottest.modules.ai.websearch.WebSearchService;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private static final int MAX_FOLLOW_UP_SUGGESTIONS = 3;
    private static final int MAX_FOLLOW_UP_LENGTH = 60;

    private final AiProperties aiProperties;
    private final ChatHistoryService chatHistoryService;
    private final ObjectMapper objectMapper;
    private final AiModelService aiModelService;
    private final Map<String, String> conversationContexts;
    private final WebSearchService webSearchService;

    @Value("${spring.ai.openai.base-url:}")
    private String springAiBaseUrl;

    @Override
    public CompletableFuture<AiChatResponse> chat(AiChatRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            log.info("Handling AI chat request: {}", request.getMessage());
            return chatWithSpringAi(request, startTime);
        });
    }

    @Override
    public void chatStream(AiChatRequest request, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            try {
                chatStreamWithSpringAi(request, emitter);
            } catch (Exception e) {
                log.error("Streaming AI chat request failed", e);
                sendEmitterError(emitter, e.getMessage(), e);
            }
        });
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

                        try {
                            List<String> suggestions = generateFollowUpSuggestions(
                                    request.getMessage(),
                                    assistantMessage,
                                    searchContext,
                                    resolved
                            );
                            sendSuggestionsEvent(emitter, suggestions);
                        } catch (Exception e) {
                            log.warn("Failed to generate follow-up suggestions", e);
                            sendSuggestionsEvent(emitter, Collections.emptyList());
                        } finally {
                            emitter.complete();
                        }
                    }
            );
        } catch (Exception e) {
            log.error("Spring AI streaming request failed", e);
            sendEmitterError(emitter, "AI request failed: " + e.getMessage(), e);
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

    private void sendSuggestionsEvent(SseEmitter emitter, List<String> suggestions) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("suggestions", suggestions == null ? Collections.emptyList() : suggestions);
            emitter.send(SseEmitter.event()
                    .name("suggestions")
                    .data(objectMapper.writeValueAsString(payload)));
        } catch (Exception e) {
            log.warn("Failed to send suggestions SSE event", e);
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
                return ResolvedChatModel.unavailable("The selected model configuration is incomplete. Please check Base URL and API Key.");
            }
            boolean useDeepThinking = Boolean.TRUE.equals(request.getUseDeepThinking())
                    && Boolean.TRUE.equals(selectedModel.getSupportsDeepThinking());
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
                    .useWebSearch(Boolean.TRUE.equals(request.getUseWebSearch()))
                    .build();
        }

        String fallbackApiKey = trimToNull(aiProperties.getOpenai().getApiKey());
        String fallbackBaseUrl = resolveFallbackBaseUrl();
        String fallbackModelName = aiModelService.resolveFallbackModelName(request.getUseDeepThinking(), false);

        if (!StringUtils.hasText(fallbackApiKey) || !StringUtils.hasText(fallbackBaseUrl) || !StringUtils.hasText(fallbackModelName)) {
            return ResolvedChatModel.unavailable("No available model was found. Please enable a default model or complete configuration.");
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
        messages.add(new UserMessage(buildUserMessage(request.getMessage(), searchContext)));
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

    private List<String> generateFollowUpSuggestions(String userMessage,
                                                     String assistantMessage,
                                                     WebSearchContext searchContext,
                                                     ResolvedChatModel resolved) {
        if (!StringUtils.hasText(assistantMessage)) {
            return Collections.emptyList();
        }

        List<Message> messages = List.of(
                new SystemMessage("""
                        You generate concise Chinese follow-up questions for a chat UI.
                        Return only a JSON array of up to 3 strings.
                        Each item must be a strong next-turn question closely tied to the answer.
                        Do not include answers, explanations, markdown, or extra wrapper text.
                        """),
                new UserMessage(buildFollowUpSuggestionPrompt(userMessage, assistantMessage, searchContext))
        );

        Prompt prompt = new Prompt(messages, buildSuggestionOptions(resolved));
        ChatResponse response = resolved.chatModel().call(prompt);
        return parseFollowUpSuggestions(extractResponseText(response), objectMapper);
    }

    private String buildFollowUpSuggestionPrompt(String userMessage,
                                                 String assistantMessage,
                                                 WebSearchContext searchContext) {
        StringBuilder builder = new StringBuilder();
        builder.append("Generate 3 Chinese follow-up questions for the next user turn.\n");
        builder.append("Requirements:\n");
        builder.append("1. Questions must stay strongly related to the assistant answer.\n");
        builder.append("2. Avoid repeating the user's original wording.\n");
        builder.append("3. Avoid generic prompts such as asking for more details without focus.\n");
        builder.append("4. If web search context exists, you may use it to make the questions more specific.\n");
        builder.append("5. Output only a JSON array, for example [\"follow-up 1\",\"follow-up 2\",\"follow-up 3\"].\n\n");
        builder.append("User question:\n").append(defaultIfBlank(userMessage, "")).append("\n\n");
        builder.append("Assistant answer:\n").append(defaultIfBlank(assistantMessage, "")).append("\n\n");

        String searchSummary = buildSearchSummary(searchContext);
        if (StringUtils.hasText(searchSummary)) {
            builder.append("Web search context:\n").append(searchSummary);
        }
        return builder.toString();
    }

    private String buildSearchSummary(WebSearchContext searchContext) {
        if (searchContext == null || !searchContext.success()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(searchContext.searchQuery())) {
            builder.append("Search query: ").append(searchContext.searchQuery()).append("\n");
        }

        List<WebSearchSource> sources = searchContext.sources();
        if (sources == null || sources.isEmpty()) {
            return builder.toString().trim();
        }

        int limit = Math.min(sources.size(), 3);
        for (int i = 0; i < limit; i++) {
            WebSearchSource source = sources.get(i);
            builder.append("Source ").append(i + 1).append(":\n");
            if (StringUtils.hasText(source.getTitle())) {
                builder.append("Title: ").append(source.getTitle()).append("\n");
            }
            if (StringUtils.hasText(source.getDomain())) {
                builder.append("Domain: ").append(source.getDomain()).append("\n");
            }
            if (StringUtils.hasText(source.getSnippet())) {
                builder.append("Snippet: ").append(source.getSnippet()).append("\n");
            }
        }
        return builder.toString().trim();
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

    private OpenAiChatOptions buildSuggestionOptions(ResolvedChatModel resolved) {
        return OpenAiChatOptions.builder()
                .model(resolved.modelName())
                .temperature(0.4)
                .maxTokens(220)
                .build();
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

    static List<String> parseFollowUpSuggestions(String raw, ObjectMapper objectMapper) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }

        String normalized = stripCodeFence(raw).trim();
        List<String> parsed = parseSuggestionJsonArray(normalized, objectMapper);
        if (!parsed.isEmpty()) {
            return sanitizeFollowUpSuggestions(parsed);
        }

        return sanitizeFollowUpSuggestions(extractSuggestionLines(normalized));
    }

    private static List<String> parseSuggestionJsonArray(String raw, ObjectMapper objectMapper) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }

        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start < 0 || end < start) {
            return Collections.emptyList();
        }

        String jsonArray = raw.substring(start, end + 1);
        try {
            return objectMapper.readValue(jsonArray, new TypeReference<List<String>>() { });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static List<String> extractSuggestionLines(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }

        List<String> suggestions = new ArrayList<>();
        for (String line : raw.split("\\r?\\n")) {
            String candidate = line.trim();
            if (!StringUtils.hasText(candidate)) {
                continue;
            }
            candidate = candidate.replaceFirst("^[-*\\s]+", "");
            candidate = candidate.replaceFirst("^\\u95EE\\u9898\\s*\\d+\\s*[:\\uFF1A]\\s*", "");
            candidate = candidate.replaceFirst("^\\u5EFA\\u8BAE\\u8FFD\\u95EE\\s*[:\\uFF1A]\\s*", "");
            candidate = candidate.replaceFirst("^\\d+[.\\u3001\\)\\uFF09]\\s*", "");
            candidate = candidate.replaceFirst("^[\\u4E00\\u4E8C\\u4E09\\u56DB\\u4E94\\u516D\\u4E03\\u516B\\u4E5D\\u5341]+[\\u3001.\\uFF0E]\\s*", "");
            candidate = candidate.replaceFirst("^[\"'\\u201C\\u201D\\u2018\\u2019]+", "");
            candidate = candidate.replaceFirst("[\"'\\u201C\\u201D\\u2018\\u2019]+$", "");
            if (StringUtils.hasText(candidate)) {
                suggestions.add(candidate);
            }
        }
        return suggestions;
    }

    private static List<String> sanitizeFollowUpSuggestions(List<String> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String suggestion : suggestions) {
            String normalized = normalizeSuggestion(suggestion);
            if (StringUtils.hasText(normalized)) {
                unique.add(normalized);
            }
            if (unique.size() >= MAX_FOLLOW_UP_SUGGESTIONS) {
                break;
            }
        }
        return new ArrayList<>(unique);
    }

    private static String normalizeSuggestion(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String normalized = value.trim();
        normalized = normalized.replaceFirst("^[-*\\s]+", "");
        normalized = normalized.replaceFirst("^\\u95EE\\u9898\\s*\\d+\\s*[:\\uFF1A]\\s*", "");
        normalized = normalized.replaceFirst("^\\u5EFA\\u8BAE\\u8FFD\\u95EE\\s*[:\\uFF1A]\\s*", "");
        normalized = normalized.replaceFirst("^\\d+[.\\u3001\\)\\uFF09]\\s*", "");
        normalized = normalized.replaceFirst("^[\\u4E00\\u4E8C\\u4E09\\u56DB\\u4E94\\u516D\\u4E03\\u516B\\u4E5D\\u5341]+[\\u3001.\\uFF0E]\\s*", "");
        normalized = normalized.replaceFirst("^[\"'\\u201C\\u201D\\u2018\\u2019]+", "");
        normalized = normalized.replaceFirst("[\"'\\u201C\\u201D\\u2018\\u2019]+$", "");
        normalized = normalized.trim();

        if (!StringUtils.hasText(normalized) || normalized.length() < 2) {
            return null;
        }
        if (normalized.length() > MAX_FOLLOW_UP_LENGTH) {
            normalized = normalized.substring(0, MAX_FOLLOW_UP_LENGTH).trim();
        }
        return normalized;
    }

    private static String stripCodeFence(String raw) {
        return raw
                .replace("```json", "")
                .replace("```JSON", "")
                .replace("```", "");
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
