package com.example.springboottest.modules.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.common.dto.PageResult;
import com.example.springboottest.modules.auth.entity.User;
import com.example.springboottest.modules.auth.repository.UserRepository;
import com.example.springboottest.modules.log.annotation.Log;
import com.example.springboottest.modules.log.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息的增删改查等管理接口")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "查询用户列表", description = "分页查询用户列表，支持按用户名、邮箱和状态筛选")
    @GetMapping
    public ApiResponse<PageResult<User>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "用户名（模糊查询）") @RequestParam(required = false) String username,
            @Parameter(description = "邮箱（模糊查询）") @RequestParam(required = false) String email,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {

        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        if (StringUtils.hasText(email)) {
            wrapper.like(User::getEmail, email);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> result = userRepository.selectPage(pageParam, wrapper);

        // 清除密码信息
        result.getRecords().forEach(u -> u.setPassword(null));

        PageResult<User> pageResult = new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize());

        return ApiResponse.success(pageResult);
    }

    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{id}")
    public ApiResponse<User> getById(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        User user = userRepository.selectById(id);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    @Operation(summary = "创建用户", description = "创建新用户账号")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ApiResponse<User> create(
            @Parameter(description = "用户信息", required = true) @RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername()) > 0) {
            return ApiResponse.error("用户名已存在");
        }
        if (userRepository.existsByEmail(user.getEmail()) > 0) {
            return ApiResponse.error("邮箱已存在");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.insert(user);

        user.setPassword(null);
        return ApiResponse.success("创建成功", user);
    }

    @Operation(summary = "更新用户", description = "更新用户信息")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public ApiResponse<User> update(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "用户信息", required = true) @RequestBody User user) {
        User existingUser = userRepository.selectById(id);
        if (existingUser == null) {
            return ApiResponse.error("用户不存在");
        }

        user.setId(id);
        user.setUpdatedAt(LocalDateTime.now());
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }

        userRepository.updateById(user);

        user.setPassword(null);
        return ApiResponse.success("更新成功", user);
    }

    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        User user = userRepository.selectById(id);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        userRepository.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary = "更新用户状态", description = "更新用户的启用/禁用状态")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "状态信息", required = true) @RequestBody Map<String, Integer> body) {
        User user = userRepository.selectById(id);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }

        Integer status = body.get("status");
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.updateById(user);

        return ApiResponse.success("状态更新成功", null);
    }

    @Operation(summary = "重置密码", description = "重置用户密码为默认密码123456")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        User user = userRepository.selectById(id);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }

        user.setPassword(passwordEncoder.encode("123456"));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.updateById(user);

        return ApiResponse.success("密码已重置为: 123456", null);
    }
}
