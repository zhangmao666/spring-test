package com.example.springboottest.modules.ai.skills;

import lombok.Data;

/**
 * Agent Skills 聊天请求 DTO
 */
@Data
public class AgentChatRequest {
    
    /**
     * 用户消息内容
     */
    private String message;
    
    /**
     * 会话ID（可选，用于多轮对话追踪）
     */
    private String conversationId;

    /**
     * 模型路由名（可选，对应 agent.models 中的 key，如 default / fast / thinking）
     * <p>为空时使用 agent.default-model 配置的模型</p>
     */
    private String model;

    /**
     * 是否使用流式输出（可选，默认 false）
     */
    private Boolean stream;
}
