package com.example.springboottest.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 高分作文生成响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EssayGenerateResponse {

    private String topic;

    private String title;

    private String essay;

    private List<String> scoreHighlights;

    private Integer estimatedWordCount;

    private String conversationId;

    private String provider;

    private String model;

    private Integer tokensUsed;

    private Long responseTime;

    private String rawContent;
}
