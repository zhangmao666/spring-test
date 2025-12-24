package com.example.springboottest.modules.task.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.common.dto.PageResult;
import com.example.springboottest.modules.task.dto.*;
import com.example.springboottest.modules.task.service.TaskService;
import com.example.springboottest.annotation.CurrentUser;
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

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<TaskVO> createTask(@Valid @RequestBody CreateTaskRequest request, @CurrentUser Long userId) {
        TaskVO task = taskService.createTask(request, userId);
        return ApiResponse.success("任务创建成功", task);
    }

    @PostMapping("/{taskId}/submit")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<TaskVO> submitTask(@PathVariable Long taskId, @CurrentUser Long userId) {
        TaskVO task = taskService.submitTask(taskId, userId);
        return ApiResponse.success("任务已提交审批", task);
    }

    @PostMapping("/{taskId}/withdraw")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<TaskVO> withdrawTask(@PathVariable Long taskId, @RequestBody WithdrawRequest request, @CurrentUser Long userId) {
        TaskVO task = taskService.withdrawTask(taskId, userId, request.getReason());
        return ApiResponse.success("任务已撤回", task);
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<TaskDetailVO> getTaskDetail(@PathVariable Long taskId, @CurrentUser Long userId) {
        TaskDetailVO detail = taskService.getTaskDetail(taskId, userId);
        return ApiResponse.success(detail);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResult<TaskVO>> getMyPendingTasks(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, @CurrentUser Long userId) {
        PageResult<TaskVO> result = taskService.getMyPendingTasks(userId, page, size);
        return ApiResponse.success(result);
    }

    @GetMapping("/created")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResult<TaskVO>> getMyCreatedTasks(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, @CurrentUser Long userId) {
        PageResult<TaskVO> result = taskService.getMyCreatedTasks(userId, page, size);
        return ApiResponse.success(result);
    }

}
