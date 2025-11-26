package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API客户端实体类
 * 用于存储第三方应用的认证信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("api_clients")
public class ApiClient {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 客户端ID（唯一标识）
     */
    @TableField("client_id")
    private String clientId;
    
    /**
     * 客户端密钥（加密存储）
     */
    @TableField("client_secret")
    private String clientSecret;
    
    /**
     * API Key（用于API Key认证）
     */
    @TableField("api_key")
    private String apiKey;
    
    /**
     * 客户端名称
     */
    @TableField("client_name")
    private String clientName;
    
    /**
     * 权限范围（逗号分隔，如：read,write）
     */
    @TableField("scopes")
    private String scopes;
    
    /**
     * Token过期时间（毫秒）
     */
    @TableField("token_expiration")
    private Long tokenExpiration;
    
    /**
     * 客户端状态（1:启用，0:禁用）
     */
    @TableField("status")
    private Integer status = 1;
    
    /**
     * 描述信息
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
     * 构造函数（创建新客户端）
     */
    public ApiClient(String clientId, String clientSecret, String clientName, 
                     String scopes, Long tokenExpiration, String description) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientName = clientName;
        this.scopes = scopes;
        this.tokenExpiration = tokenExpiration;
        this.status = 1;
        this.description = description;
    }
}
