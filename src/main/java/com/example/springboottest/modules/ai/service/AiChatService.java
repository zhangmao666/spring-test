package com.example.springboottest.modules.ai.service;

import com.example.springboottest.entity.DTO.AiChatRequest;
import com.example.springboottest.entity.DTO.AiChatResponse;
import com.example.springboottest.entity.DTO.AiProviderInfo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AiChatService {

    /**
     * 发送聊天消息
     */
    CompletableFuture<AiChatResponse> chat(AiChatRequest request);

    /**
     * 流式聊天消息
     */
    void chatStream(AiChatRequest request, SseEmitter emitter);

    /**
     * 获取可用的AI提供商列表
     */
    List<AiProviderInfo> getAvailableProviders();

    /**
     * 检查指定提供商是否可用
     */
    boolean isProviderAvailable(String provider);
}
