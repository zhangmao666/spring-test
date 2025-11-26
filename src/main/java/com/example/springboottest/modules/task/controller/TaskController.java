package com.example.springboottest.modules.task.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.common.dto.PageResult;
import com.example.springboottest.modules.task.dto.*;
import com.example.springboottest.modules.task.service.TaskService;
import com.example.springboottest.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 任务控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<TaskVO> createTask(@Valid @RequestBody CreateTaskRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        TaskVO task = taskService.createTask(request, userId);
        return ApiResponse.success("任务创建成功", task);
    }

    @PostMapping("/{taskId}/submit")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<TaskVO> submitTask(@PathVariable Long taskId, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        TaskVO task = taskService.submitTask(taskId, userId);
        return ApiResponse.success("任务已提交审批", task);
    }

    @PostMapping("/{taskId}/withdraw")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<TaskVO> withdrawTask(@PathVariable Long taskId, @RequestBody WithdrawRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        TaskVO task = taskService.withdrawTask(taskId, userId, request.getReason());
        return ApiResponse.success("任务已撤回", task);
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<TaskDetailVO> getTaskDetail(@PathVariable Long taskId, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        TaskDetailVO detail = taskService.getTaskDetail(taskId, userId);
        return ApiResponse.success(detail);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResult<TaskVO>> getMyPendingTasks(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        PageResult<TaskVO> result = taskService.getMyPendingTasks(userId, page, size);
        return ApiResponse.success(result);
    }

    @GetMapping("/created")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResult<TaskVO>> getMyCreatedTasks(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        PageResult<TaskVO> result = taskService.getMyCreatedTasks(userId, page, size);
        return ApiResponse.success(result);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = getJwtFromRequest(request);
        if (token != null) {
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("无法获取当前用户信息");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
