package com.example.springboottest.modules.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 选课记录DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollDTO {
    
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private LocalDateTime enrollTime;
}
