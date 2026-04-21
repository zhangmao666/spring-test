package com.example.springboottest.modules.ai.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.entity.DTO.AiChatRequest;
import com.example.springboottest.entity.DTO.AiChatResponse;
import com.example.springboottest.entity.DTO.AiProviderInfo;
import com.example.springboottest.modules.ai.dto.AiCapabilitiesResponse;
import com.example.springboottest.modules.ai.dto.EssayGenerateRequest;
import com.example.springboottest.modules.ai.dto.EssayGenerateResponse;
import com.example.springboottest.modules.ai.dto.ResumeGenerateRequest;
import com.example.springboottest.modules.ai.dto.ResumeGenerateResponse;
import com.example.springboottest.modules.ai.dto.ResumeOptimizeRequest;
import com.example.springboottest.modules.ai.dto.ResumeOptimizeResponse;
import com.example.springboottest.modules.ai.service.AiChatService;
import com.example.springboottest.modules.ai.websearch.WebSearchService;
import com.example.springboottest.modules.prompt.service.PromptTemplateService;
import com.example.springboottest.modules.prompt.support.PromptTemplateCodes;
import com.example.springboottest.modules.prompt.support.PromptTemplateDefaults;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final PromptTemplateService promptTemplateService;
    private final WebSearchService webSearchService;
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

    @Operation(summary = "Get AI capabilities", description = "Returns lightweight capability flags for the chat client")
    @GetMapping("/capabilities")
    public ApiResponse<AiCapabilitiesResponse> getCapabilities() {
        return ApiResponse.success(AiCapabilitiesResponse.builder()
                .webSearchEnabled(webSearchService.isEnabled())
                .webSearchMode("system-searxng")
                .build());
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
        Map<String, Object> variables = new HashMap<>();
        variables.put("topic", request.getTopic());
        variables.put("gradeLevel", request.getGradeLevel());
        variables.put("genre", request.getGenre());
        variables.put("expectedWordCount", request.getExpectedWordCount());
        variables.put("requirementsBlock", buildLineBlock("补充要求", request.getRequirements()));
        return promptTemplateService.renderPromptWithFallback(
                PromptTemplateCodes.AI_ESSAY_HIGH_SCORE,
                PromptTemplateDefaults.ESSAY_HIGH_SCORE,
                variables
        );
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
        Map<String, Object> variables = new HashMap<>();
        variables.put("targetPosition", request.getTargetPosition());
        variables.put("targetIndustryBlock", buildSectionBlock("目标行业", request.getTargetIndustry()));
        variables.put("optimizeDirectionBlock", buildSectionBlock("优化方向", request.getOptimizeDirection()));
        variables.put("additionalRequirementsBlock", buildSectionBlock("附加要求", request.getAdditionalRequirements()));
        variables.put("resumeContent", safeText(request.getResumeContent()));
        return promptTemplateService.renderPromptWithFallback(
                PromptTemplateCodes.AI_RESUME_OPTIMIZE,
                PromptTemplateDefaults.RESUME_OPTIMIZE,
                variables
        );
    }

    private String buildResumeGeneratePrompt(ResumeGenerateRequest request) {
        String styleDesc = switch (request.getStyle() == null ? "detailed" : request.getStyle()) {
            case "concise" -> "简洁风格，每项不超过2行";
            case "technical" -> "技术向风格，突出技术栈和量化数据";
            default -> "详细风格，内容充实，量化成果";
        };

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", request.getName());
        variables.put("targetPosition", request.getTargetPosition());
        variables.put("targetIndustryBlock", buildLineBlock("目标行业", request.getTargetIndustry()));
        variables.put("workYears", request.getWorkYears());
        variables.put("educationBlock", buildLineBlock("学历", request.getEducation()));
        variables.put("schoolBlock", buildLineBlock("毕业院校", request.getSchool()));
        variables.put("majorBlock", buildLineBlock("专业", request.getMajor()));
        variables.put("coreSkillsBlock", buildContentSection("核心技能", request.getCoreSkills()));
        variables.put("workExperienceBlock", buildContentSection("工作经历（关键信息）", request.getWorkExperience()));
        variables.put("projectExperienceBlock", buildContentSection("项目经历（关键信息）", request.getProjectExperience()));
        variables.put("personalSummaryBlock", buildContentSection("个人优势", request.getPersonalSummary()));
        variables.put("additionalInfoBlock", buildContentSection("其他信息", request.getAdditionalInfo()));
        variables.put("styleDesc", styleDesc);
        return promptTemplateService.renderPromptWithFallback(
                PromptTemplateCodes.AI_RESUME_GENERATE,
                PromptTemplateDefaults.RESUME_GENERATE,
                variables
        );
    }

    private String buildLineBlock(String label, String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return label + "：" + value.trim() + "\n";
    }

    private String buildSectionBlock(String label, String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return "【" + label + "】" + value.trim() + "\n";
    }

    private String buildContentSection(String title, String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        return "\n【" + title + "】\n" + content.trim() + "\n";
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
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
