package com.example.springboottest.modules.ai.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.entity.DTO.AiChatRequest;
import com.example.springboottest.entity.DTO.AiChatResponse;
import com.example.springboottest.entity.DTO.AiProviderInfo;
import com.example.springboottest.modules.ai.dto.EssayGenerateRequest;
import com.example.springboottest.modules.ai.dto.EssayGenerateResponse;
import com.example.springboottest.modules.ai.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Validated
@Tag(name = "AI聊天", description = "AI对话、模型管理、作文生成等接口")
public class AiChatController {

    private final AiChatService aiChatService;
    private static final int ESSAY_TIMEOUT_SECONDS = 180;

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
                        return ApiResponse.success(response);
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

        SseEmitter emitter = new SseEmitter(300000L);
        aiChatService.chatStream(request, emitter);
        return emitter;
    }

    @Operation(summary = "生成高分作文", description = "根据题目、年级、体裁和补充要求生成高分作文，并返回得分亮点")
    @PostMapping("/essay/high-score")
    public CompletableFuture<ApiResponse<EssayGenerateResponse>> generateHighScoreEssay(
            @Parameter(description = "高分作文生成请求", required = true) @Valid @RequestBody EssayGenerateRequest request) {
        String conversationId = StringUtils.hasText(request.getConversationId())
                ? request.getConversationId().trim()
                : UUID.randomUUID().toString();

        AiChatRequest aiRequest = AiChatRequest.builder()
                .message(buildHighScoreEssayPrompt(request))
                .conversationId(conversationId)
                .useDeepThinking(Boolean.TRUE.equals(request.getUseDeepThinking()))
                .useWebSearch(Boolean.TRUE.equals(request.getUseWebSearch()))
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens())
                .build();

        return aiChatService.chat(aiRequest)
                .completeOnTimeout(
                        AiChatResponse.builder()
                                .success(false)
                                .error("作文生成超时，请稍后重试或降低字数/关闭深度思考")
                                .responseTime((long) ESSAY_TIMEOUT_SECONDS * 1000)
                                .build(),
                        ESSAY_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                )
                .thenApply(aiResponse -> {
                    if (!aiResponse.isSuccess()) {
                        return ApiResponse.<EssayGenerateResponse>error(500, aiResponse.getError());
                    }

                    EssayGenerateResponse response = parseEssayResponse(request.getTopic(), conversationId, aiResponse);
                    return ApiResponse.success("高分作文生成成功", response);
                })
                .exceptionally(ex -> {
                    log.error("高分作文生成失败", ex);
                    return ApiResponse.<EssayGenerateResponse>error(500, "高分作文生成失败: " + ex.getMessage());
                });
    }

    private String buildHighScoreEssayPrompt(EssayGenerateRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名资深中高考语文阅卷老师和作文教练，请生成一篇可作为高分范文的作文。").append('\n');
        prompt.append("题目：").append(request.getTopic()).append('\n');
        prompt.append("年级：").append(request.getGradeLevel()).append('\n');
        prompt.append("体裁：").append(request.getGenre()).append('\n');
        prompt.append("目标字数：约").append(request.getExpectedWordCount()).append("字").append('\n');

        if (StringUtils.hasText(request.getRequirements())) {
            prompt.append("补充要求：").append(request.getRequirements().trim()).append('\n');
        }

        prompt.append("写作要求：立意积极深刻、结构完整、论证或叙事充分、语言有文采、避免空话套话。").append('\n');
        prompt.append("输出格式必须严格如下，不要增加其它小节：").append('\n');
        prompt.append("【作文标题】").append('\n');
        prompt.append("（给出一个正式且有吸引力的标题）").append('\n');
        prompt.append("【作文正文】").append('\n');
        prompt.append("（完整作文正文）").append('\n');
        prompt.append("【得分亮点】").append('\n');
        prompt.append("1. ...").append('\n');
        prompt.append("2. ...").append('\n');
        prompt.append("3. ...");
        return prompt.toString();
    }

    private EssayGenerateResponse parseEssayResponse(String topic, String conversationId, AiChatResponse aiResponse) {
        String rawContent = aiResponse.getMessage() == null ? "" : aiResponse.getMessage().trim();
        String title = extractTitle(rawContent);
        String essay = extractEssayBody(rawContent);
        List<String> highlights = extractHighlights(rawContent);

        if (!StringUtils.hasText(title)) {
            title = topic;
        }
        if (!StringUtils.hasText(essay)) {
            essay = rawContent;
        }
        if (highlights.isEmpty()) {
            highlights.add("立意明确，紧扣题目并具备一定深度");
            highlights.add("结构清晰，开头、主体、结尾完整");
            highlights.add("语言流畅，有较好的表达与文采");
        }

        return EssayGenerateResponse.builder()
                .topic(topic)
                .title(title)
                .essay(essay)
                .scoreHighlights(highlights)
                .estimatedWordCount(estimateWordCount(essay))
                .conversationId(conversationId)
                .provider(aiResponse.getProvider())
                .model(aiResponse.getModel())
                .tokensUsed(aiResponse.getTokensUsed())
                .responseTime(aiResponse.getResponseTime())
                .rawContent(rawContent)
                .build();
    }

    private String extractTitle(String content) {
        return matchFirstGroup(content, "【(?:作文标题|标题)】\\s*([^\\r\\n]+)");
    }

    private String extractEssayBody(String content) {
        String body = matchFirstGroup(content, "【(?:作文正文|正文)】\\s*([\\s\\S]*?)(?=【(?:得分亮点|亮点)】|$)");
        return body == null ? null : body.trim();
    }

    private List<String> extractHighlights(String content) {
        List<String> highlights = new ArrayList<>();
        String block = matchFirstGroup(content, "【(?:得分亮点|亮点)】\\s*([\\s\\S]*)$");
        if (!StringUtils.hasText(block)) {
            return highlights;
        }

        String[] lines = block.split("\\r?\\n");
        for (String line : lines) {
            String item = line.replaceFirst("^[\\s\\-•\\d\\.、]+", "").trim();
            if (StringUtils.hasText(item)) {
                highlights.add(item);
            }
        }
        return highlights;
    }

    private String matchFirstGroup(String content, String regex) {
        if (!StringUtils.hasText(content)) {
            return null;
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private Integer estimateWordCount(String content) {
        if (!StringUtils.hasText(content)) {
            return 0;
        }
        return content.replaceAll("\\s+", "").length();
    }
}
