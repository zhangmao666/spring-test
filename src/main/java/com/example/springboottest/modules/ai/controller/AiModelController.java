package com.example.springboottest.modules.ai.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.ai.dto.AiModelResponse;
import com.example.springboottest.modules.ai.dto.AiModelTestRequest;
import com.example.springboottest.modules.ai.dto.AiModelTestResponse;
import com.example.springboottest.modules.ai.dto.AiModelUpsertRequest;
import com.example.springboottest.modules.ai.setvice.AiModelService;
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

import java.util.List;

@RestController
@RequestMapping("/ai/models")
@RequiredArgsConstructor
@Tag(name = "AI模型管理", description = "OpenAI协议模型管理接口")
public class AiModelController {

    private final AiModelService aiModelService;

    @GetMapping
    @Operation(summary = "获取模型列表")
    public ApiResponse<List<AiModelResponse>> listModels(@RequestParam(defaultValue = "false") boolean enabledOnly) {
        return ApiResponse.success(enabledOnly ? aiModelService.listEnabledModels() : aiModelService.listModels());
    }

    @GetMapping("/registry")
    @Operation(summary = "获取智能体模型注册表")
    public ApiResponse<List<AiModelResponse>> listAgentModelRegistry() {
        return ApiResponse.success(aiModelService.listEnabledModels());
    }

    @PostMapping("/test")
    @Operation(summary = "测试模型连接")
    public ApiResponse<AiModelTestResponse> testConnection(@Valid @RequestBody AiModelTestRequest request) {
        AiModelTestResponse response = aiModelService.testConnection(request);
        if (Boolean.TRUE.equals(response.getSuccess())) {
            return ApiResponse.success("连接测试成功", response);
        }
        return ApiResponse.error(500, response.getMessage());
    }

    @PostMapping
    @Operation(summary = "新建模型")
    public ApiResponse<Long> createModel(@Valid @RequestBody AiModelUpsertRequest request) {
        return ApiResponse.success("模型创建成功", aiModelService.createAiModel(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新模型")
    public ApiResponse<Void> updateModel(@PathVariable Long id, @Valid @RequestBody AiModelUpsertRequest request) {
        aiModelService.updateAiModel(id, request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "设置默认模型")
    public ApiResponse<Void> setDefault(@PathVariable Long id) {
        aiModelService.markAsDefault(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新模型状态")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        aiModelService.updateStatus(id, enabled);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模型")
    public ApiResponse<Void> deleteModel(@PathVariable Long id) {
        aiModelService.deleteModel(id);
        return ApiResponse.success();
    }
}
