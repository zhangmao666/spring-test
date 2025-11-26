package com.example.springboottest.service;

import com.example.springboottest.DTO.AiChatRequest;
import com.example.springboottest.DTO.AiChatResponse;
import com.example.springboottest.DTO.AiProviderInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AiChatService {
    
    /**
     * 发送聊天消息
     */
    CompletableFuture<AiChatResponse> chat(AiChatRequest request);
    
    /**
     * 获取可用的AI提供商列表
     */
    List<AiProviderInfo> getAvailableProviders();
    
    /**
     * 检查指定提供商是否可用
     */
    boolean isProviderAvailable(String provider);
}