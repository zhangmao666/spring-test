package com.example.springboottest.modules.course.dto;

import com.example.springboottest.modules.course.enums.CourseUserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建课程系统用户请求
 */
@Data
public class CreateCourseUserRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50之间")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
    private String password;
    
    @NotNull(message = "角色不能为空")
    private CourseUserRole role;
}
