package com.example.springboottest.modules.ai.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI对话消息实体（MyBatis POJO）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private Long id;

    /**
     * 所属会话ID
     */
    private String conversationId;

    /**
     * 消息角色：user / assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 思考过程（如果有深度思考功能）
     */
    private String thought;

    /**
     * 思考时长（秒）
     */
    private Integer thinkingTime;

    /**
     * 是否使用了联网搜索
     */
    private Boolean usedWebSearch;

    /**
     * 是否使用了深度思考
     */
    private Boolean usedDeepThinking;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 消息在会话中的序号
     */
    private Integer messageIndex;
}
