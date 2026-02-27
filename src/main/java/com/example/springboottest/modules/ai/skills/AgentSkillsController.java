package com.example.springboottest.modules.ai.skills;

import com.example.springboottest.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * Agent Skills Controller
 *
 * <h3>核心能力：</h3>
 * <ul>
 *   <li>多模型路由对话（同步 / SSE 流式）</li>
 *   <li>动态 Skill 管理（列表 / 创建 / 删除）</li>
 *   <li>可用模型查询</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/ai/agent")
@RequiredArgsConstructor
@Tag(name = "AI Agent Skills", description = "Agent 智能对话 + Skill 管理")
public class AgentSkillsController {

    private final AgentService agentService;
    private final SkillGeneratorService skillGeneratorService;

    // ==================== 对话接口 ====================

    /**
     * Agent 对话（同步 / 流式自动选择）
     */
    @Operation(summary = "Agent智能对话",
            description = "支持多模型路由和 SSE 流式输出。设置 stream=true 返回 SSE 流")
    @PostMapping("/chat")
    public Object chat(@RequestBody AgentChatRequest request) {
        if (Boolean.TRUE.equals(request.getStream())) {
            return chatStream(request);
        }

        AgentChatResponse response = agentService.chat(request.getMessage(), request.getModel());

        if (response.isSuccess()) {
            return ApiResponse.success(response);
        } else {
            return ApiResponse.error(500, response.getError());
        }
    }

    /**
     * Agent 流式对话（SSE）
     */
    @Operation(summary = "Agent流式对话", description = "通过 SSE 实时推送 Agent 回复")
    @PostMapping(value = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AgentChatRequest request) {
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时
        agentService.chatStream(request.getMessage(), request.getModel(), emitter);
        return emitter;
    }

    /**
     * 快速提问（GET）
     */
    @Operation(summary = "快速提问(GET)", description = "通过URL参数快速向Agent提问")
    @GetMapping("/ask")
    public ApiResponse<AgentChatResponse> quickAsk(
            @Parameter(description = "问题内容", example = "查一下系统信息") @RequestParam("q") String question,
            @Parameter(description = "模型路由名", example = "default") @RequestParam(value = "model", required = false) String model) {

        AgentChatResponse response = agentService.chat(question, model);
        return response.isSuccess() ? ApiResponse.success(response) : ApiResponse.error(500, response.getError());
    }

    // ==================== 模型管理 ====================

    /**
     * 获取可用模型列表
     */
    @Operation(summary = "获取可用模型列表", description = "返回当前 Agent 可使用的所有模型")
    @GetMapping("/models")
    public ApiResponse<List<Map<String, String>>> listModels() {
        return ApiResponse.success(agentService.getAvailableModels());
    }

    // ==================== Skill 管理 ====================

    /**
     * 获取所有 Skill 列表（内置 + 自定义）
     */
    @Operation(summary = "获取技能列表", description = "返回所有已注册的 Skill（内置 + 自定义）")
    @GetMapping("/skills")
    public ApiResponse<List<SkillGeneratorService.SkillInfo>> listSkills() {
        return ApiResponse.success(skillGeneratorService.listSkills());
    }

    /**
     * 生成自定义 Skill
     */
    @Operation(summary = "生成自定义Skill", description = "通过自然语言描述动态生成 Skill")
    @PostMapping("/skills")
    public ApiResponse<SkillGeneratorService.SkillInfo> generateSkill(
            @RequestBody SkillGeneratorService.GenerateSkillRequest request) {
        SkillGeneratorService.SkillInfo info = skillGeneratorService.generateSkill(request);
        return ApiResponse.success(info);
    }

    /**
     * 删除自定义 Skill
     */
    @Operation(summary = "删除自定义Skill", description = "仅允许删除自定义目录中的 Skill")
    @DeleteMapping("/skills/{skillName}")
    public ApiResponse<Void> deleteSkill(
            @Parameter(description = "Skill 名称") @PathVariable String skillName) {
        boolean deleted = skillGeneratorService.deleteSkill(skillName);
        if (deleted) {
            return ApiResponse.success(null);
        } else {
            return ApiResponse.error(404, "Skill 不存在: " + skillName);
        }
    }
}
