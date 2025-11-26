package com.example.springboottest.modules.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.springboottest.modules.course.enums.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("courses")
public class Course {
    
    /**
     * 课程ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 授课教师ID
     */
    @TableField("teacher_id")
    private Long teacherId;
    
    /**
     * 课程价格
     */
    @TableField("price")
    private BigDecimal price;
    
    /**
     * 课程状态：DRAFT, PUBLISHED
     */
    @TableField("status")
    private CourseStatus status;
    
    /**
     * 课程描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 授课教师（非数据库字段）
     */
    @TableField(exist = false)
    private CourseUser teacher;
}
