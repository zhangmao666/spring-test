package com.example.springboottest.modules.ai.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.entity.DTO.AiChatRequest;
import com.example.springboottest.entity.DTO.AiChatResponse;
import com.example.springboottest.entity.DTO.AiProviderInfo;
import com.example.springboottest.modules.ai.dto.EssayGenerateRequest;
import com.example.springboottest.modules.ai.dto.EssayGenerateResponse;
import com.example.springboottest.modules.ai.dto.ResumeGenerateRequest;
import com.example.springboottest.modules.ai.dto.ResumeGenerateResponse;
import com.example.springboottest.modules.ai.dto.ResumeOptimizeRequest;
import com.example.springboottest.modules.ai.dto.ResumeOptimizeResponse;
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

    // ═══════════════════════════════════════════════════════════
    //  简历优化接口
    // ═══════════════════════════════════════════════════════════

    private static final int RESUME_TIMEOUT_SECONDS = 180;

    @Operation(summary = "AI简历优化", description = "对已有简历内容进行智能优化，提升匹配度和表达质量")
    @PostMapping("/resume/optimize")
    public CompletableFuture<ApiResponse<ResumeOptimizeResponse>> optimizeResume(
            @Parameter(description = "简历优化请求", required = true) @Valid @RequestBody ResumeOptimizeRequest request) {
        log.info("收到简历优化请求，目标岗位: {}", request.getTargetPosition());

        String conversationId = StringUtils.hasText(request.getConversationId())
                ? request.getConversationId().trim()
                : UUID.randomUUID().toString();

        AiChatRequest aiRequest = AiChatRequest.builder()
                .message(buildResumeOptimizePrompt(request))
                .conversationId(conversationId)
                .useDeepThinking(Boolean.TRUE.equals(request.getUseDeepThinking()))
                .useWebSearch(false)
                .temperature(0.6)
                .maxTokens(3000)
                .build();

        return aiChatService.chat(aiRequest)
                .completeOnTimeout(
                        AiChatResponse.builder()
                                .success(false)
                                .error("简历优化超时，请稍后重试")
                                .responseTime((long) RESUME_TIMEOUT_SECONDS * 1000)
                                .build(),
                        RESUME_TIMEOUT_SECONDS, TimeUnit.SECONDS
                )
                .thenApply(aiResponse -> {
                    if (!aiResponse.isSuccess()) {
                        return ApiResponse.<ResumeOptimizeResponse>error(500, aiResponse.getError());
                    }
                    ResumeOptimizeResponse response = parseResumeOptimizeResponse(request, conversationId, aiResponse);
                    return ApiResponse.success("简历优化完成", response);
                })
                .exceptionally(ex -> {
                    log.error("简历优化失败", ex);
                    return ApiResponse.<ResumeOptimizeResponse>error(500, "简历优化失败: " + ex.getMessage());
                });
    }

    @Operation(summary = "AI简历生成", description = "根据用户基本信息和经历，AI全自动生成专业简历")
    @PostMapping("/resume/generate")
    public CompletableFuture<ApiResponse<ResumeGenerateResponse>> generateResume(
            @Parameter(description = "简历生成请求", required = true) @Valid @RequestBody ResumeGenerateRequest request) {
        log.info("收到简历生成请求，目标岗位: {}", request.getTargetPosition());

        String conversationId = StringUtils.hasText(request.getConversationId())
                ? request.getConversationId().trim()
                : UUID.randomUUID().toString();

        AiChatRequest aiRequest = AiChatRequest.builder()
                .message(buildResumeGeneratePrompt(request))
                .conversationId(conversationId)
                .useDeepThinking(Boolean.TRUE.equals(request.getUseDeepThinking()))
                .useWebSearch(false)
                .temperature(0.7)
                .maxTokens(3000)
                .build();

        return aiChatService.chat(aiRequest)
                .completeOnTimeout(
                        AiChatResponse.builder()
                                .success(false)
                                .error("简历生成超时，请稍后重试")
                                .responseTime((long) RESUME_TIMEOUT_SECONDS * 1000)
                                .build(),
                        RESUME_TIMEOUT_SECONDS, TimeUnit.SECONDS
                )
                .thenApply(aiResponse -> {
                    if (!aiResponse.isSuccess()) {
                        return ApiResponse.<ResumeGenerateResponse>error(500, aiResponse.getError());
                    }
                    ResumeGenerateResponse response = parseResumeGenerateResponse(request, conversationId, aiResponse);
                    return ApiResponse.success("简历生成成功", response);
                })
                .exceptionally(ex -> {
                    log.error("简历生成失败", ex);
                    return ApiResponse.<ResumeGenerateResponse>error(500, "简历生成失败: " + ex.getMessage());
                });
    }

    // ─── Prompt 构建 ───────────────────────────────────────────

    private String buildResumeOptimizePrompt(ResumeOptimizeRequest request) {
        StringBuilder p = new StringBuilder();
        p.append("你是一名专业的简历优化顾问和职场导师，请对以下简历进行全面优化。\n\n");
        p.append("【目标岗位】").append(request.getTargetPosition()).append("\n");
        if (StringUtils.hasText(request.getTargetIndustry())) {
            p.append("【目标行业】").append(request.getTargetIndustry()).append("\n");
        }
        if (StringUtils.hasText(request.getOptimizeDirection())) {
            p.append("【优化方向】").append(request.getOptimizeDirection()).append("\n");
        }
        if (StringUtils.hasText(request.getAdditionalRequirements())) {
            p.append("【附加要求】").append(request.getAdditionalRequirements()).append("\n");
        }
        p.append("\n【原始简历内容】\n").append(request.getResumeContent()).append("\n\n");
        p.append("请按以下格式严格输出，不要增加其它小节：\n");
        p.append("【匹配度评分】\n（给出0-100的整数评分，并简要说明原因）\n");
        p.append("【优化后简历】\n（输出优化后的完整简历正文，保持清晰的板块结构）\n");
        p.append("【优化摘要】\n1. ...\n2. ...\n3. ...\n");
        p.append("【核心亮点】\n1. ...\n2. ...\n3. ...\n");
        p.append("【改进建议】\n1. ...\n2. ...\n3. ...");
        return p.toString();
    }

    private String buildResumeGeneratePrompt(ResumeGenerateRequest request) {
        StringBuilder p = new StringBuilder();
        p.append("你是一名资深HR和职业规划师，请根据以下信息生成一份专业的中文简历。\n\n");
        p.append("【基本信息】\n");
        p.append("姓名：").append(request.getName()).append("\n");
        p.append("目标岗位：").append(request.getTargetPosition()).append("\n");
        if (StringUtils.hasText(request.getTargetIndustry())) {
            p.append("目标行业：").append(request.getTargetIndustry()).append("\n");
        }
        p.append("工作年限：").append(request.getWorkYears()).append("\n");
        if (StringUtils.hasText(request.getEducation())) {
            p.append("学历：").append(request.getEducation()).append("\n");
        }
        if (StringUtils.hasText(request.getSchool())) {
            p.append("毕业院校：").append(request.getSchool()).append("\n");
        }
        if (StringUtils.hasText(request.getMajor())) {
            p.append("专业：").append(request.getMajor()).append("\n");
        }
        if (StringUtils.hasText(request.getCoreSkills())) {
            p.append("\n【核心技能】\n").append(request.getCoreSkills()).append("\n");
        }
        if (StringUtils.hasText(request.getWorkExperience())) {
            p.append("\n【工作经历（关键信息）】\n").append(request.getWorkExperience()).append("\n");
        }
        if (StringUtils.hasText(request.getProjectExperience())) {
            p.append("\n【项目经历（关键信息）】\n").append(request.getProjectExperience()).append("\n");
        }
        if (StringUtils.hasText(request.getPersonalSummary())) {
            p.append("\n【个人优势】\n").append(request.getPersonalSummary()).append("\n");
        }
        if (StringUtils.hasText(request.getAdditionalInfo())) {
            p.append("\n【其他信息】\n").append(request.getAdditionalInfo()).append("\n");
        }
        String styleDesc = switch (request.getStyle() == null ? "detailed" : request.getStyle()) {
            case "concise" -> "简洁风格，每项不超过2行";
            case "technical" -> "技术向风格，突出技术栈和量化数据";
            default -> "详细风格，内容充实，量化成果";
        };
        p.append("\n【写作要求】\n");
        p.append("风格：").append(styleDesc).append("\n");
        p.append("要求：使用 Markdown 格式，结构清晰，量化描述工作成果，突出与目标岗位的匹配度。\n");
        p.append("必须包含：个人简介、工作经历、项目经历（如有）、技能特长、教育背景板块。\n\n");
        p.append("请按以下格式严格输出：\n");
        p.append("【简历正文】\n（Markdown 格式的完整简历）\n");
        p.append("【写作建议】\n（3-5条简历优化或求职建议）");
        return p.toString();
    }

    // ─── 响应解析 ──────────────────────────────────────────────

    private ResumeOptimizeResponse parseResumeOptimizeResponse(
            ResumeOptimizeRequest request, String conversationId, AiChatResponse aiResponse) {
        String raw = aiResponse.getMessage() == null ? "" : aiResponse.getMessage().trim();

        // 匹配度评分
        Integer score = null;
        String scoreBlock = matchFirstGroup(raw, "【匹配度评分】\\s*([\\s\\S]*?)(?=【|$)");
        if (StringUtils.hasText(scoreBlock)) {
            Matcher m = Pattern.compile("(\\d{1,3})").matcher(scoreBlock);
            if (m.find()) {
                score = Math.min(100, Integer.parseInt(m.group(1)));
            }
        }

        String optimized = extractSection(raw, "【优化后简历】", "【优化摘要】");
        List<String> summary = extractNumberedList(raw, "【优化摘要】");
        List<String> highlights = extractNumberedList(raw, "【核心亮点】");
        List<String> suggestions = extractNumberedList(raw, "【改进建议】");

        if (!StringUtils.hasText(optimized)) optimized = raw;

        return ResumeOptimizeResponse.builder()
                .targetPosition(request.getTargetPosition())
                .optimizedResume(optimized)
                .optimizeSummary(summary)
                .highlights(highlights)
                .suggestions(suggestions)
                .matchScore(score)
                .conversationId(conversationId)
                .provider(aiResponse.getProvider())
                .model(aiResponse.getModel())
                .tokensUsed(aiResponse.getTokensUsed())
                .responseTime(aiResponse.getResponseTime())
                .rawContent(raw)
                .build();
    }

    private ResumeGenerateResponse parseResumeGenerateResponse(
            ResumeGenerateRequest request, String conversationId, AiChatResponse aiResponse) {
        String raw = aiResponse.getMessage() == null ? "" : aiResponse.getMessage().trim();

        String resume = extractSection(raw, "【简历正文】", "【写作建议】");
        String tips = matchFirstGroup(raw, "【写作建议】\\s*([\\s\\S]*)$");

        if (!StringUtils.hasText(resume)) resume = raw;

        return ResumeGenerateResponse.builder()
                .name(request.getName())
                .targetPosition(request.getTargetPosition())
                .resumeContent(resume)
                .writingTips(tips)
                .conversationId(conversationId)
                .provider(aiResponse.getProvider())
                .model(aiResponse.getModel())
                .tokensUsed(aiResponse.getTokensUsed())
                .responseTime(aiResponse.getResponseTime())
                .rawContent(raw)
                .build();
    }

    private String extractSection(String content, String startTag, String endTag) {
        if (!StringUtils.hasText(content)) return null;
        Pattern p = Pattern.compile(
                Pattern.quote(startTag) + "\\s*([\\s\\S]*?)(?=" + Pattern.quote(endTag) + "|$)");
        Matcher m = p.matcher(content);
        return m.find() ? m.group(1).trim() : null;
    }

    private List<String> extractNumberedList(String content, String sectionTag) {
        List<String> result = new ArrayList<>();
        String block = matchFirstGroup(content,
                Pattern.quote(sectionTag) + "\\s*([\\s\\S]*?)(?=【|$)");
        if (!StringUtils.hasText(block)) return result;
        for (String line : block.split("\\r?\\n")) {
            String item = line.replaceFirst("^[\\s\\-•\\d\\.、]+", "").trim();
            if (StringUtils.hasText(item)) result.add(item);
        }
        return result;
    }
}
