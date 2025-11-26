package com.example.springboottest.modules.course.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.course.dto.CourseUserDTO;
import com.example.springboottest.modules.course.dto.CreateCourseUserRequest;
import com.example.springboottest.modules.course.service.CourseUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程系统用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/course-users")
@RequiredArgsConstructor
public class CourseUserController {
    
    private final CourseUserService courseUserService;
    
    /**
     * 创建用户
     */
    @PostMapping
    public ApiResponse<CourseUserDTO> createUser(@Valid @RequestBody CreateCourseUserRequest request) {
        log.info("创建课程系统用户: username={}, role={}", request.getUsername(), request.getRole());
        CourseUserDTO user = courseUserService.createUser(request);
        return ApiResponse.success("用户创建成功", user);
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    public ApiResponse<CourseUserDTO> getUser(@PathVariable Long userId) {
        var user = courseUserService.getUserById(userId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        return ApiResponse.success(CourseUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build());
    }
    
    /**
     * 获取所有用户
     */
    @GetMapping
    public ApiResponse<List<CourseUserDTO>> listUsers() {
        return ApiResponse.success(courseUserService.listUsers());
    }
    
    /**
     * 获取所有教师
     */
    @GetMapping("/teachers")
    public ApiResponse<List<CourseUserDTO>> listTeachers() {
        return ApiResponse.success(courseUserService.listTeachers());
    }
    
    /**
     * 获取所有学生
     */
    @GetMapping("/students")
    public ApiResponse<List<CourseUserDTO>> listStudents() {
        return ApiResponse.success(courseUserService.listStudents());
    }
    
    /**
     * 启用用户
     */
    @PutMapping("/{userId}/enable")
    public ApiResponse<String> enableUser(@PathVariable Long userId) {
        courseUserService.enableUser(userId);
        return ApiResponse.success("用户已启用");
    }
    
    /**
     * 禁用用户
     */
    @PutMapping("/{userId}/disable")
    public ApiResponse<String> disableUser(@PathVariable Long userId) {
        courseUserService.disableUser(userId);
        return ApiResponse.success("用户已禁用");
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public ApiResponse<String> deleteUser(@PathVariable Long userId) {
        courseUserService.deleteUser(userId);
        return ApiResponse.success("用户已删除");
    }
}
