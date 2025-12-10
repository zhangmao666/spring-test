package com.example.springboottest.controller;

import com.example.springboottest.DTO.AiChatRequest;
import com.example.springboottest.DTO.AiChatResponse;
import com.example.springboottest.DTO.AiProviderInfo;
import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Validated
public class AiChatController {
    
    private final AiChatService aiChatService;
    
    @PostMapping("/chatAi")
    public CompletableFuture<ApiResponse<AiChatResponse>> chat(@Valid @RequestBody AiChatRequest request) {
        log.info("收到AI聊天请求: {}", request.getMessage());
        
        // 如果没有提供对话ID，生成一个新的
        if (request.getConversationId() == null) {
            request.setConversationId(UUID.randomUUID().toString());
        }
        
        return aiChatService.chat(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    return ApiResponse.<AiChatResponse>success(response);
                } else {
                    return ApiResponse.<AiChatResponse>error(500, response.getError());
                }
            })
            .exceptionally(ex -> {
                log.error("AI聊天请求处理失败", ex);
                return ApiResponse.<AiChatResponse>error(500, "AI服务暂时不可用");
            });
    }
    
    @GetMapping("/providers")
    public ApiResponse<List<AiProviderInfo>> getProviders() {
        List<AiProviderInfo> providers = aiChatService.getAvailableProviders();
        return ApiResponse.success(providers);
    }
    
    @GetMapping("/providers/{provider}/status")
    public ApiResponse<Boolean> checkProviderStatus(@PathVariable String provider) {
        boolean available = aiChatService.isProviderAvailable(provider);
        return ApiResponse.success(available);
    }
}