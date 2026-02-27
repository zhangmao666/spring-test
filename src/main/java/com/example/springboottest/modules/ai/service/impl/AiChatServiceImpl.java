package com.example.springboottest.modules.ai.service.impl;

import com.example.springboottest.config.AiProperties;
import com.example.springboottest.entity.DTO.AiChatRequest;
import com.example.springboottest.entity.DTO.AiChatResponse;
import com.example.springboottest.entity.DTO.AiProviderInfo;
import com.example.springboottest.modules.ai.service.AiChatService;
import com.example.springboottest.modules.ai.service.ChatHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiProperties aiProperties;
    private final ChatHistoryService chatHistoryService;
    private final ObjectMapper objectMapper;

    // 注入不同的 ChatModel
    private final OpenAiChatModel defaultOpenAiChatModel;

    @Qualifier("thinkingChatModel")
    private final OpenAiChatModel thinkingChatModel;

    @Qualifier("searchChatModel")
    private final OpenAiChatModel searchChatModel;

    private final Map<String, String> conversationContexts;

    @Override
    public CompletableFuture<AiChatResponse> chat(AiChatRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            log.info("处理AI聊天请求，消息: {}", request.getMessage());
            return chatWithSpringAi(request, startTime);
        });
    }

    /**
     * 使用 Spring AI 进行非流式聊天
     */
    private AiChatResponse chatWithSpringAi(AiChatRequest request, long startTime) {
        if (aiProperties.getOpenai().getApiKey() == null || aiProperties.getOpenai().getApiKey().trim().isEmpty()) {
            return AiChatResponse.builder()
                .success(false)
                .error("OpenAI API密钥未配置")
                .responseTime(System.currentTimeMillis() - startTime)
                .build();
        }

        try {
            // 1. 选择合适的模型
            OpenAiChatModel chatModel = selectChatModel(request);

            // 2. 构建消息列表
            List<Message> messages = buildMessages(request);

            // 3. 构建选项（支持 web_search 工具）
            OpenAiChatOptions options = buildChatOptions(request);

            // 4. 创建 Prompt 并调用
            Prompt prompt = new Prompt(messages, options);
            ChatResponse response = chatModel.call(prompt);

            // 5. 解析响应并返回
            return parseSpringAiResponse(response, request, startTime);

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

    @Override
    public void chatStream(AiChatRequest request, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            try {
                chatStreamWithSpringAi(request, emitter);
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

    /**
     * 使用 Spring AI 进行流式聊天
     */
    private void chatStreamWithSpringAi(AiChatRequest request, SseEmitter emitter) {
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
            // 保存用户消息到历史记录
            try {
                chatHistoryService.saveUserMessage(
                    request.getConversationId(),
                    request.getMessage(),
                    Boolean.TRUE.equals(request.getUseWebSearch()),
                    Boolean.TRUE.equals(request.getUseDeepThinking())
                );
            } catch (Exception e) {
                log.warn("保存用户消息失败", e);
            }

            // 1. 选择合适的模型
            OpenAiChatModel chatModel = selectChatModel(request);

            // 2. 构建消息和选项
            List<Message> messages = buildMessages(request);
            OpenAiChatOptions options = buildChatOptions(request);
            Prompt prompt = new Prompt(messages, options);

            // 3. 调用流式 API
            Flux<ChatResponse> flux = chatModel.stream(prompt);

            // 4. 转换为 SSE 并发送
            StringBuilder fullResponse = new StringBuilder();
            StringBuilder thoughtContent = new StringBuilder();
            long thinkingStartTime = System.currentTimeMillis();
            final Integer[] thinkingTimeSeconds = {null};

            flux.subscribe(
                chatResponse -> {
                    try {
                        // 获取内容
                        String content = chatResponse.getResult().getOutput().getText();
                        if (content != null && !content.isEmpty()) {
                            fullResponse.append(content);

                            // 转换为 SSE 格式（保持前端兼容）
                            String sseData = convertToSseFormat(chatResponse);
                            emitter.send(SseEmitter.event()
                                .name("message")
                                .data(sseData));
                        }
                    } catch (Exception e) {
                        log.error("发送流式消息失败", e);
                    }
                },
                error -> {
                    log.error("流式调用失败", error);
                    try {
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"error\":\"" + error.getMessage() + "\"}"));
                        emitter.completeWithError(error);
                    } catch (Exception ex) {
                        log.error("发送错误消息失败", ex);
                    }
                },
                () -> {
                    try {
                        // 更新会话上下文
                        updateConversationContext(request.getConversationId(), request.getMessage(), fullResponse.toString());

                        // 保存 AI 回复到历史记录
                        try {
                            chatHistoryService.saveAssistantMessage(
                                request.getConversationId(),
                                fullResponse.toString(),
                                thoughtContent.length() > 0 ? thoughtContent.toString() : null,
                                thinkingTimeSeconds[0]
                            );
                        } catch (Exception e) {
                            log.warn("保存 AI 回复失败", e);
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
            try {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"error\":\"AI请求失败: " + e.getMessage() + "\"}"));
                emitter.completeWithError(e);
            } catch (Exception ex) {
                log.error("发送错误消息失败", ex);
            }
        }
    }

    /**
     * 选择合适的 ChatModel
     */
    private OpenAiChatModel selectChatModel(AiChatRequest request) {
        if (Boolean.TRUE.equals(request.getUseDeepThinking())) {
            log.debug("使用深度思考模型: {}", aiProperties.getOpenai().getThinkingModel());
            return thinkingChatModel;
        } else if (Boolean.TRUE.equals(request.getUseWebSearch())) {
            log.debug("使用联网搜索模型: {}", aiProperties.getOpenai().getSearchModel());
            return searchChatModel;
        } else {
            log.debug("使用默认模型: {}", aiProperties.getOpenai().getModel());
            return defaultOpenAiChatModel;
        }
    }

    /**
     * 构建消息列表
     */
    private List<Message> buildMessages(AiChatRequest request) {
        List<Message> messages = new ArrayList<>();

        // 添加会话上下文（如果存在）
        if (request.getConversationId() != null && !request.getConversationId().trim().isEmpty()) {
            String context = conversationContexts.get(request.getConversationId());
            if (context != null && !context.trim().isEmpty()) {
                messages.add(new SystemMessage(context));
            }
        }

        // 添加用户消息
        messages.add(new UserMessage(request.getMessage()));

        return messages;
    }

    /**
     * 构建 ChatOptions（包含 tools 配置）
     */
    private OpenAiChatOptions buildChatOptions(AiChatRequest request) {
        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder();

        // 设置基本参数
        if (request.getMaxTokens() != null) {
            builder.maxTokens(request.getMaxTokens());
        }
        if (request.getTemperature() != null) {
            builder.temperature(request.getTemperature());
        }

        // 添加联网搜索工具（如果需要）
        if (Boolean.TRUE.equals(request.getUseWebSearch())) {
            // 注意：Spring AI 的工具配置方式可能需要根据实际 API 调整
            // 这里使用 OpenAI 兼容的方式
            Map<String, Object> toolConfig = new HashMap<>();
            toolConfig.put("type", "web_search");
            // builder.withTools(List.of(toolConfig)); // 根据实际 Spring AI API 调整
        }

        return builder.build();
    }

    /**
     * 解析 Spring AI 响应
     */
    private AiChatResponse parseSpringAiResponse(ChatResponse response, AiChatRequest request, long startTime) {
        try {
            String message = response.getResult().getOutput().getText();

            // 获取 token 使用情况
            Integer tokensUsed = null;
            if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                var totalTokens = response.getMetadata().getUsage().getTotalTokens();
                tokensUsed = (int) totalTokens;
            }

            // 更新会话上下文
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

        } catch (Exception e) {
            log.error("解析Spring AI响应失败", e);
            return AiChatResponse.builder()
                .success(false)
                .error("解析响应失败: " + e.getMessage())
                .provider("openai")
                .responseTime(System.currentTimeMillis() - startTime)
                .build();
        }
    }

    /**
     * 转换为 SSE 格式（保持前端兼容）
     */
    private String convertToSseFormat(ChatResponse chatResponse) {
        try {
            Map<String, Object> sseData = new HashMap<>();

            // 构建与原格式兼容的数据结构
            Map<String, Object> choice = new HashMap<>();
            Map<String, Object> delta = new HashMap<>();

            String content = chatResponse.getResult().getOutput().getText();
            if (content != null) {
                delta.put("content", content);
            }

            choice.put("delta", delta);
            sseData.put("choices", List.of(choice));

            return objectMapper.writeValueAsString(sseData);
        } catch (Exception e) {
            log.error("转换SSE格式失败", e);
            return "{\"choices\":[{\"delta\":{\"content\":\"\"}}]}";
        }
    }

    /**
     * 更新会话上下文
     */
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
}
