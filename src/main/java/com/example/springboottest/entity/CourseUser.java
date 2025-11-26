package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.springboottest.enums.CourseUserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程系统用户实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("course_users")
public class CourseUser {
    
    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名（唯一）
     */
    @TableField("username")
    private String username;
    
    /**
     * 密码（加密存储）
     */
    @TableField("password")
    private String password;
    
    /**
     * 角色：ADMIN, TEACHER, STUDENT
     */
    @TableField("role")
    private CourseUserRole role;
    
    /**
     * 用户状态（1:启用，0:禁用）
     */
    @TableField("status")
    private Integer status;
    
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
}
