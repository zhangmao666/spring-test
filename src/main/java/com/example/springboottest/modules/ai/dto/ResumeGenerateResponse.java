package com.example.springboottest.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 简历生成响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeGenerateResponse {

    /** 姓名 */
    private String name;

    /** 目标岗位 */
    private String targetPosition;

    /** 生成的完整简历（Markdown 格式） */
    private String resumeContent;

    /** 简历撰写建议 */
    private String writingTips;

    /** 会话ID */
    private String conversationId;

    /** AI 提供商 */
    private String provider;

    /** 使用模型 */
    private String model;

    /** 消耗 token 数 */
    private Integer tokensUsed;

    /** 响应时长（ms） */
    private Long responseTime;

    /** AI 原始输出 */
    private String rawContent;
}
