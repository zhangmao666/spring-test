package com.example.springboottest.modules.auth.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.auth.dto.LoginRequest;
import com.example.springboottest.modules.auth.dto.LoginResponse;
import com.example.springboottest.modules.auth.dto.RegisterRequest;
import com.example.springboottest.modules.auth.entity.User;
import com.example.springboottest.modules.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "认证管理", description = "用户登录、注册等认证相关接口")
public class AuthController {

    private final UserService userService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "通过用户名和密码进行登录认证，成功后返回JWT令牌")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Parameter(description = "登录请求信息，包含用户名和密码", required = true) @Valid @RequestBody LoginRequest loginRequest) {
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
    @Operation(summary = "用户注册", description = "创建新用户账号，需要提供用户名、密码和邮箱")
    @PostMapping("/register")
    public ApiResponse<String> register(
            @Parameter(description = "注册请求信息，包含用户名、密码和邮箱", required = true) @Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = userService.register(
                    registerRequest.getUsername(),
                    registerRequest.getPassword(),
                    registerRequest.getEmail());
            return ApiResponse.success("注册成功", "用户ID: " + user.getId());
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage());
            return ApiResponse.error("注册失败: " + e.getMessage());
        }
    }
}
