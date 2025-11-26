package com.example.springboottest.controller;

import com.example.springboottest.DTO.*;
import com.example.springboottest.service.TaskApprovalFlowService;
import com.example.springboottest.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批流配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/approval-flows")
@RequiredArgsConstructor
public class TaskApprovalFlowController {

    private final TaskApprovalFlowService flowService;
    private final JwtUtil jwtUtil;

    /**
     * 创建审批流
     * POST /api/approval-flows
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TaskApprovalFlowVO> createFlow(
            @Valid @RequestBody CreateFlowRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getCurrentUserId(httpRequest);
        TaskApprovalFlowVO flow = flowService.createFlow(request, userId);
        return ApiResponse.success("审批流创建成功", flow);
    }

    /**
     * 更新审批流
     * PUT /api/approval-flows/{flowId}
     */
    @PutMapping("/{flowId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TaskApprovalFlowVO> updateFlow(
            @PathVariable Long flowId,
            @Valid @RequestBody UpdateFlowRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getCurrentUserId(httpRequest);
        TaskApprovalFlowVO flow = flowService.updateFlow(flowId, request, userId);
        return ApiResponse.success("审批流更新成功", flow);
    }

    /**
     * 获取审批流详情
     * GET /api/approval-flows/{flowId}
     */
    @GetMapping("/{flowId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<TaskApprovalFlowDetailVO> getFlowDetail(
            @PathVariable Long flowId
    ) {
        TaskApprovalFlowDetailVO detail = flowService.getFlowDetail(flowId);
        return ApiResponse.success(detail);
    }

    /**
     * 获取审批流列表
     * GET /api/approval-flows
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<List<TaskApprovalFlowVO>> listFlows(
            @RequestParam(required = false) String taskType
    ) {
        List<TaskApprovalFlowVO> flows = flowService.listFlows(taskType);
        return ApiResponse.success(flows);
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
