package com.example.springboottest.modules.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.springboottest.modules.auth.enums.Gender;
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
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("username")
    private String username;

    @TableField("gender")
    private Gender gender;
    
    @TableField("password")
    private String password;
    
    @TableField("email")
    private String email;
    
    @TableField("status")
    private Integer status = 1;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.status = 1;
    }
}
