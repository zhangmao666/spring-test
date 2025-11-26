package com.example.springboottest.modules.course.dto;

import com.example.springboottest.modules.course.enums.CourseUserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程系统用户DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseUserDTO {
    
    private Long id;
    private String username;
    private CourseUserRole role;
    private Integer status;
    private LocalDateTime createdAt;
}
