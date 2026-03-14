package com.example.springboottest.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 简历优化响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeOptimizeResponse {

    /** 目标岗位 */
    private String targetPosition;

    /** 优化后的完整简历文本 */
    private String optimizedResume;

    /** 优化摘要说明列表 */
    private List<String> optimizeSummary;

    /** 核心亮点列表（3-5条） */
    private List<String> highlights;

    /** 改进建议列表 */
    private List<String> suggestions;

    /** 匹配度评分（0-100） */
    private Integer matchScore;

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

    /** AI 原始输出（调试用） */
    private String rawContent;
}
