package com.example.springboottest.modules.auth.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.auth.dto.LoginRequest;
import com.example.springboottest.modules.auth.dto.LoginResponse;
import com.example.springboottest.modules.auth.dto.RegisterRequest;
import com.example.springboottest.modules.auth.entity.User;
import com.example.springboottest.modules.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    
    private final UserService userService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = userService.login(loginRequest);
            return ApiResponse.success("登录成功", loginResponse);
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage());
            return ApiResponse.error("登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = userService.register(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getEmail()
            );
            return ApiResponse.success("注册成功", "用户ID: " + user.getId());
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage());
            return ApiResponse.error("注册失败: " + e.getMessage());
        }
    }
}
