package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.springboottest.controller.ProgressController;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {
    
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

    @TableField("gender")
    private ProgressController.Gender gender;
    
    /**
     * 密码（加密存储）
     */
    @TableField("password")
    private String password;
    
    /**
     * 邮箱（唯一）
     */
    @TableField("email")
    private String email;
    
    /**
     * 用户状态（1:启用，0:禁用）
     */
    @TableField("status")
    private Integer status = 1;
    
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
     * 构造函数（用于创建新用户）
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.status = 1;
    }

}
