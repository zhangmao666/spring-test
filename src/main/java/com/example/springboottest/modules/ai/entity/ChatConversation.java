package com.example.springboottest.modules.ai.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI对话会话实体（MyBatis POJO）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversation {

    private Long id;

    /**
     * 会话ID（前端生成的UUID）
     */
    private String conversationId;

    /**
     * 会话标题（取第一条用户消息或自动生成）
     */
    private String title;

    /**
     * 用户ID（如果有用户系统）
     */
    private String userId;

    /**
     * AI服务提供商
     */
    private String provider;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 最后一条消息时间（用于排序）
     */
    private LocalDateTime lastMessageTime;

    /**
     * 消息总数
     */
    private Integer messageCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 是否已删除
     */
    private Boolean deleted;
}
