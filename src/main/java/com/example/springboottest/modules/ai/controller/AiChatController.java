package com.example.springboottest.modules.ai.controller;

import com.example.springboottest.entity.DTO.AiChatRequest;
import com.example.springboottest.entity.DTO.AiChatResponse;
import com.example.springboottest.entity.DTO.AiProviderInfo;
import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.ai.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Validated
@Tag(name = "AI聊天", description = "AI对话、模型管理等接口")
public class AiChatController {

    private final AiChatService aiChatService;

    @Operation(summary = "AI聊天", description = "发送消息给AI并获取回复（非流式）")
    @PostMapping("/chatAi")
    public CompletableFuture<ApiResponse<AiChatResponse>> chat(
            @Parameter(description = "AI聊天请求", required = true) @Valid @RequestBody AiChatRequest request) {
        log.info("收到AI聊天请求: {}", request.getMessage());

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

    @Operation(summary = "获取AI模型列表", description = "获取所有可用的AI服务提供商列表")
    @GetMapping("/providers")
    public ApiResponse<List<AiProviderInfo>> getProviders() {
        List<AiProviderInfo> providers = aiChatService.getAvailableProviders();
        return ApiResponse.success(providers);
    }

    @Operation(summary = "检查AI模型状态", description = "检查指定AI服务提供商是否可用")
    @GetMapping("/providers/{provider}/status")
    public ApiResponse<Boolean> checkProviderStatus(
            @Parameter(description = "AI服务提供商名称", required = true) @PathVariable String provider) {
        boolean available = aiChatService.isProviderAvailable(provider);
        return ApiResponse.success(available);
    }

    @Operation(summary = "AI流式聊天", description = "发送消息给AI并获取SSE流式回复")
    @PostMapping(value = "/chatStream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @Parameter(description = "AI聊天请求", required = true) @Valid @RequestBody AiChatRequest request) {
        log.info("收到AI流式聊天请求: {}", request.getMessage());

        if (request.getConversationId() == null) {
            request.setConversationId(UUID.randomUUID().toString());
        }

        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        aiChatService.chatStream(request, emitter);

        return emitter;
    }
}
