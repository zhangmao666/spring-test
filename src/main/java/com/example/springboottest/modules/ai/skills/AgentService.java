package com.example.springboottest.modules.ai.skills;

import com.example.springboottest.config.AiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 核心服务
 * <p>
 * 提供多模型路由、同步/流式对话、工具调用等能力。
 * 所有模型共享同一个 yunwu.ai 代理，通过 options 覆盖 model 字段来切换模型。
 * </p>
 */
@Slf4j
@Service
public class AgentService {

    private final ChatClient agentChatClient;
    private final ToolCallback[] agentToolCallbacks;
    private final AgentProperties agentProperties;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final OpenAiChatModel defaultOpenAiChatModel;

    public AgentService(@Qualifier("agentChatClient") ChatClient agentChatClient,
                        @Qualifier("agentToolCallbacks") ToolCallback[] agentToolCallbacks,
                        AgentProperties agentProperties,
                        AiProperties aiProperties,
                        ObjectMapper objectMapper, OpenAiChatModel defaultOpenAiChatModel) {
        this.agentChatClient = agentChatClient;
        this.agentToolCallbacks = agentToolCallbacks;
        this.agentProperties = agentProperties;
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.defaultOpenAiChatModel = defaultOpenAiChatModel;
    }

    /**
     * 同步对话（使用默认模型或指定模型）
     *
     * @param message   用户消息
     * @param modelKey  模型路由名（如 default / fast / thinking），null 则用默认
     * @return Agent 回复
     */
    public AgentChatResponse chat(String message, String modelKey) {
        long startTime = System.currentTimeMillis();
        log.info("🤖 收到 Agent 聊天请求: {} | 模型: {}", message, modelKey);

        try {
            String reply;


            // 使用默认模型
            reply = agentChatClient.prompt()
                    .user(message)
                    .call()
                    .content();


            long elapsed = System.currentTimeMillis() - startTime;
            log.info("🤖 Agent 回复完成 | 耗时: {}ms", elapsed);

            return AgentChatResponse.builder()
                    .reply(reply)
                    .responseTimeMs(elapsed)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("🤖 Agent 处理失败", e);
            long elapsed = System.currentTimeMillis() - startTime;
            return AgentChatResponse.builder()
                    .success(false)
                    .error("Agent处理失败: " + e.getMessage())
                    .responseTimeMs(elapsed)
                    .build();
        }
    }

    /**
     * 流式对话（SSE）
     *
     * @param message   用户消息
     * @param modelKey  模型路由名
     * @param emitter   SSE 发射器
     */
    public void chatStream(String message, String modelKey, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            log.info("🤖 收到 Agent 流式请求: {} | 模型: {}", message, modelKey);

            try {

                var promptSpec = agentChatClient.prompt().user(message);

                // 使用流式调用
                promptSpec.stream().chatResponse().subscribe(
                        chatResponse -> {
                            try {
                                String content = chatResponse.getResult().getOutput().getText();
                                if (content != null && !content.isEmpty()) {
                                    Map<String, Object> sseData = new HashMap<>();
                                    Map<String, Object> choice = new HashMap<>();
                                    Map<String, Object> delta = new HashMap<>();
                                    delta.put("content", content);
                                    choice.put("delta", delta);
                                    sseData.put("choices", List.of(choice));

                                    emitter.send(SseEmitter.event()
                                            .name("message")
                                            .data(objectMapper.writeValueAsString(sseData)));
                                }
                            } catch (Exception e) {
                                log.error("发送流式消息失败", e);
                            }
                        },
                        error -> {
                            log.error("Agent 流式调用失败", error);
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
                                long elapsed = System.currentTimeMillis() - startTime;
                                log.info("🤖 Agent 流式回复完成 | 耗时: {}ms", elapsed);
                                emitter.send(SseEmitter.event()
                                        .name("done")
                                        .data("{\"status\":\"completed\",\"responseTimeMs\":" + elapsed + "}"));
                                emitter.complete();
                            } catch (Exception e) {
                                log.error("完成流式响应失败", e);
                            }
                        }
                );

            } catch (Exception e) {
                log.error("Agent 流式请求失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"error\":\"Agent请求失败: " + e.getMessage() + "\"}"));
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error("发送错误消息失败", ex);
                }
            }
        });
    }

}
