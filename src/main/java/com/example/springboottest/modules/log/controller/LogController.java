package com.example.springboottest.modules.log.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.log.entity.LoginLog;
import com.example.springboottest.modules.log.entity.OperationLog;
import com.example.springboottest.modules.log.service.LoginLogService;
import com.example.springboottest.modules.log.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class LogController {

    private final LoginLogService loginLogService;
    private final OperationLogService operationLogService;

    // ==================== 登录日志 ====================

    /**
     * 分页查询登录日志
     */
    @GetMapping("/login/list")
    public ApiResponse<Map<String, Object>> getLoginLogList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        IPage<LoginLog> pageResult = loginLogService.getLoginLogList(page, size, username, status, startTime, endTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        
        return ApiResponse.success(result);
    }

    /**
     * 删除登录日志
     */
    @DeleteMapping("/login/{id}")
    public ApiResponse<Void> deleteLoginLog(@PathVariable Long id) {
        loginLogService.deleteLoginLog(id);
        return ApiResponse.success();
    }

    /**
     * 批量删除登录日志
     */
    @DeleteMapping("/login/batch")
    public ApiResponse<Void> batchDeleteLoginLog(@RequestBody Long[] ids) {
        loginLogService.batchDeleteLoginLog(ids);
        return ApiResponse.success();
    }

    /**
     * 清空登录日志
     */
    @DeleteMapping("/login/clear")
    public ApiResponse<Void> clearLoginLog() {
        loginLogService.clearLoginLog();
        return ApiResponse.success();
    }

    // ==================== 操作日志 ====================

    /**
     * 分页查询操作日志
     */
    @GetMapping("/operation/list")
    public ApiResponse<Map<String, Object>> getOperationLogList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        IPage<OperationLog> pageResult = operationLogService.getOperationLogList(
                page, size, title, operatorName, businessType, status, startTime, endTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        
        return ApiResponse.success(result);
    }

    /**
     * 获取操作日志详情
     */
    @GetMapping("/operation/{id}")
    public ApiResponse<OperationLog> getOperationLogDetail(@PathVariable Long id) {
        OperationLog operationLog = operationLogService.getOperationLogById(id);
        return ApiResponse.success(operationLog);
    }

    /**
     * 删除操作日志
     */
    @DeleteMapping("/operation/{id}")
    public ApiResponse<Void> deleteOperationLog(@PathVariable Long id) {
        operationLogService.deleteOperationLog(id);
        return ApiResponse.success();
    }

    /**
     * 批量删除操作日志
     */
    @DeleteMapping("/operation/batch")
    public ApiResponse<Void> batchDeleteOperationLog(@RequestBody Long[] ids) {
        operationLogService.batchDeleteOperationLog(ids);
        return ApiResponse.success();
    }

    /**
     * 清空操作日志
     */
    @DeleteMapping("/operation/clear")
    public ApiResponse<Void> clearOperationLog() {
        operationLogService.clearOperationLog();
        return ApiResponse.success();
    }
}
