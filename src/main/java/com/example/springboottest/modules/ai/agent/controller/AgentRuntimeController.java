package com.example.springboottest.modules.ai.agent.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.ai.agent.entity.AgentPermissionRequest;
import com.example.springboottest.modules.ai.agent.entity.AgentTodo;
import com.example.springboottest.modules.ai.agent.service.AgentPermissionService;
import com.example.springboottest.modules.ai.agent.service.AgentRunService;
import com.example.springboottest.modules.ai.agent.service.AgentRuntimeQueryService;
import com.example.springboottest.modules.ai.agent.service.AgentTodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/agent")
@RequiredArgsConstructor
@Tag(name = "AI Agent Runtime", description = "Agent 运行状态、Todo、恢复和权限接口")
public class AgentRuntimeController {

    private final AgentRuntimeQueryService queryService;
    private final AgentRunService runService;
    private final AgentTodoService todoService;
    private final AgentPermissionService permissionService;

    @Operation(summary = "获取 Agent run 详情")
    @GetMapping("/runs/{runId}")
    public ApiResponse<AgentRuntimeQueryService.AgentRunDetail> getRun(@PathVariable String runId) {
        return ApiResponse.success(queryService.detail(runId));
    }

    @Operation(summary = "恢复 Agent run")
    @PostMapping("/runs/{runId}/resume")
    public ApiResponse<Map<String, Object>> resumeRun(@PathVariable String runId) {
        return ApiResponse.success(Map.of(
                "runId", runId,
                "status", "RESUME_REQUESTED",
                "message", "恢复入口已预留，请通过 chatStream 携带 resumeRunId 继续执行"
        ));
    }

    @Operation(summary = "取消 Agent run")
    @PostMapping("/runs/{runId}/cancel")
    public ApiResponse<Void> cancelRun(@PathVariable String runId) {
        runService.cancel(runId);
        return ApiResponse.success();
    }

    @Operation(summary = "获取 Agent Todo 列表")
    @GetMapping("/runs/{runId}/todos")
    public ApiResponse<List<AgentTodo>> getTodos(@PathVariable String runId) {
        return ApiResponse.success(todoService.listTodos(runId));
    }

    @Operation(summary = "审批 Agent 权限请求")
    @PostMapping("/permissions/{requestId}/decision")
    public ApiResponse<AgentPermissionRequest> decidePermission(@PathVariable String requestId,
                                                                @RequestBody PermissionDecisionRequest request) {
        permissionService.decide(requestId, request != null && Boolean.TRUE.equals(request.getApproved()));
        return ApiResponse.success(permissionService.get(requestId));
    }

    @Data
    public static class PermissionDecisionRequest {
        private Boolean approved;
    }
}
