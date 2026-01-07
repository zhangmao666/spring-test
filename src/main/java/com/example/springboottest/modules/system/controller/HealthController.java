package com.example.springboottest.modules.system.controller;

import com.example.springboottest.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/health")
@Tag(name = "系统健康检查", description = "系统健康状态检查接口")
public class HealthController {

    /**
     * 健康检查接口
     */
    @Operation(summary = "健康检查", description = "检查系统运行状态")
    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("application", "Spring Boot Test");
        healthInfo.put("version", "1.0.0");

        return ApiResponse.success("服务运行正常", healthInfo);
    }
}
