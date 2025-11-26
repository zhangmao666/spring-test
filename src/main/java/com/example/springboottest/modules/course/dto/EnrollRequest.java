package com.example.springboottest.modules.course.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 选课请求
 */
@Data
public class EnrollRequest {
    
    @NotNull(message = "学生ID不能为空")
    private Long studentId;
    
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
}
