package com.example.springboottest.DTO.course;

import com.example.springboottest.enums.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    
    private Long id;
    private String name;
    private Long teacherId;
    private String teacherName;
    private BigDecimal price;
    private CourseStatus status;
    private String description;
    private Integer enrollCount;
    private LocalDateTime createdAt;
}
