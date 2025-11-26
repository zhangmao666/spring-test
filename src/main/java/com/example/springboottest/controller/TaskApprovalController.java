package com.example.springboottest.controller;

import com.example.springboottest.DTO.*;
import com.example.springboottest.service.TaskApprovalService;
import com.example.springboottest.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务审批控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks/{taskId}/approvals")
@RequiredArgsConstructor
public class TaskApprovalController {

    private final TaskApprovalService approvalService;
    private final JwtUtil jwtUtil;

    /**
     * 审批通过
     * POST /api/tasks/{taskId}/approvals/approve
     */
    @PostMapping("/approve")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<ApprovalResultVO> approve(
            @PathVariable Long taskId,
            @Valid @RequestBody ApproveRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getCurrentUserId(httpRequest);
        request.setTaskId(taskId);
        ApprovalResultVO result = approvalService.approve(request, userId);
        return ApiResponse.success("审批通过", result);
    }

    /**
     * 驳回到指定节点
     * POST /api/tasks/{taskId}/approvals/reject
     */
    @PostMapping("/reject")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<ApprovalResultVO> reject(
            @PathVariable Long taskId,
            @Valid @RequestBody RejectRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getCurrentUserId(httpRequest);
        request.setTaskId(taskId);
        ApprovalResultVO result = approvalService.reject(request, userId);
        return ApiResponse.success("任务已驳回", result);
    }

    /**
     * 转交他人审批
     * POST /api/tasks/{taskId}/approvals/transfer
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<ApprovalResultVO> transfer(
            @PathVariable Long taskId,
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getCurrentUserId(httpRequest);
        request.setTaskId(taskId);
        ApprovalResultVO result = approvalService.transfer(request, userId);
        return ApiResponse.success("审批已转交", result);
    }

    /**
     * 获取审批历史
     * GET /api/tasks/{taskId}/approvals/history
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<List<ApprovalRecordVO>> getApprovalHistory(
            @PathVariable Long taskId
    ) {
        List<ApprovalRecordVO> history = approvalService.getApprovalHistory(taskId);
        return ApiResponse.success(history);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = getJwtFromRequest(request);
        if (token != null) {
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("无法获取当前用户信息");
    }

    /**
     * 从请求头提取JWT Token
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
