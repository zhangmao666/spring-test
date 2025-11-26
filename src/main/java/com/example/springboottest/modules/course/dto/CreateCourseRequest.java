package com.example.springboottest.modules.course.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建课程请求
 */
@Data
public class CreateCourseRequest {
    
    @NotBlank(message = "课程名称不能为空")
    @Size(min = 2, max = 100, message = "课程名称长度必须在2-100之间")
    private String name;
    
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;
    
    @NotNull(message = "课程价格不能为空")
    @DecimalMin(value = "0", message = "课程价格不能为负数")
    private BigDecimal price;
    
    private String description;
}
