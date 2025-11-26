package com.example.springboottest.modules.course.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新课程请求
 */
@Data
public class UpdateCourseRequest {
    
    @Size(min = 2, max = 100, message = "课程名称长度必须在2-100之间")
    private String name;
    
    private Long teacherId;
    
    @DecimalMin(value = "0", message = "课程价格不能为负数")
    private BigDecimal price;
    
    private String description;
}
