package com.example.springboottest.modules.ai.skill.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.ai.skill.dto.AiSkillQueryRequest;
import com.example.springboottest.modules.ai.skill.dto.AiSkillRequest;
import com.example.springboottest.modules.ai.skill.dto.AiSkillResponse;
import com.example.springboottest.modules.ai.skill.dto.SkillTestRequest;
import com.example.springboottest.modules.ai.skill.dto.SkillTestResponse;
import com.example.springboottest.modules.ai.skill.service.AiSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/ai/skills")
@RequiredArgsConstructor
@Tag(name = "AI技能管理", description = "技能定义、同步和测试接口")
public class AiSkillController {

    private final AiSkillService aiSkillService;

    @GetMapping
    @Operation(summary = "获取技能列表")
    public ApiResponse<List<AiSkillResponse>> listSkills(AiSkillQueryRequest query) {
        return ApiResponse.success(aiSkillService.listSkills(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取技能详情")
    public ApiResponse<AiSkillResponse> getSkill(@PathVariable Long id) {
        return ApiResponse.success(aiSkillService.getSkill(id));
    }

    @PostMapping
    @Operation(summary = "创建技能")
    public ApiResponse<Long> createSkill(@Valid @RequestBody AiSkillRequest request) {
        return ApiResponse.success("技能创建成功", aiSkillService.createSkill(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新技能")
    public ApiResponse<Void> updateSkill(@PathVariable Long id, @Valid @RequestBody AiSkillRequest request) {
        aiSkillService.updateSkill(id, request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新技能状态")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        aiSkillService.updateStatus(id, enabled);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据库技能")
    public ApiResponse<Void> deleteSkill(@PathVariable Long id) {
        aiSkillService.deleteSkill(id);
        return ApiResponse.success();
    }

    @PostMapping("/sync-local")
    @Operation(summary = "同步本地技能")
    public ApiResponse<Integer> syncLocalSkills() {
        return ApiResponse.success("本地技能同步完成", aiSkillService.syncLocalSkills());
    }

    @PostMapping("/import-zip")
    @Operation(summary = "导入技能 zip 包")
    public ApiResponse<AiSkillResponse> importSkillZip(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success("技能导入成功", aiSkillService.importSkillZip(file));
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "测试工具型技能")
    public ApiResponse<SkillTestResponse> testSkill(@PathVariable Long id, @RequestBody SkillTestRequest request) {
        SkillTestResponse response = aiSkillService.testSkill(
                id,
                request == null ? null : request.getConversationId(),
                request == null ? null : request.getArguments()
        );
        if (Boolean.TRUE.equals(response.getSuccess())) {
            return ApiResponse.success("技能执行成功", response);
        }
        return ApiResponse.error(500, response.getError());
    }
}
