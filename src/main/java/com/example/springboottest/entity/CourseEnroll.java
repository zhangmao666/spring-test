package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 选课记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("course_enrolls")
public class CourseEnroll {
    
    /**
     * 选课记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID
     */
    @TableField("student_id")
    private Long studentId;
    
    /**
     * 课程ID
     */
    @TableField("course_id")
    private Long courseId;
    
    /**
     * 选课时间
     */
    @TableField(value = "enroll_time", fill = FieldFill.INSERT)
    private LocalDateTime enrollTime;
    
    /**
     * 课程信息（非数据库字段）
     */
    @TableField(exist = false)
    private Course course;
    
    /**
     * 学生信息（非数据库字段）
     */
    @TableField(exist = false)
    private CourseUser student;
}
