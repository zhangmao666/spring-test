package com.example.springboottest.modules.ai.skills;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Agent Skills 聊天响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatResponse {

    /**
     * AI 回复内容
     */
    private String reply;

    /**
     * 本次请求中 AI 调用的技能列表
     */
    private List<String> skillsUsed;

    /**
     * 响应耗时（毫秒）
     */
    private long responseTimeMs;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息（如果失败）
     */
    private String error;
}
