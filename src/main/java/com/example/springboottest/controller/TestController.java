package com.example.springboottest.controller;

import com.example.springboottest.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 用于测试认证和授权功能
 */
@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    /**
     * 需要认证的测试接口
     */
    @GetMapping("/auth")
    public ApiResponse<Map<String, Object>> testAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> data = new HashMap<>();
        data.put("username", authentication.getName());
        data.put("authorities", authentication.getAuthorities());
        data.put("authenticated", authentication.isAuthenticated());
        data.put("message", "您已成功通过认证！");
        
        log.info("用户 {} 访问了需要认证的接口", authentication.getName());
        
        return ApiResponse.success("认证测试成功", data);
    }

    /**
     * 公开访问的测试接口
     */
    @GetMapping("/public")
    public ApiResponse<Map<String, Object>> testPublic() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "这是一个公开访问的接口");
        data.put("timestamp", System.currentTimeMillis());
        
        log.info("公开接口被访问");
        
        return ApiResponse.success("公开接口访问成功", data);
    }
}