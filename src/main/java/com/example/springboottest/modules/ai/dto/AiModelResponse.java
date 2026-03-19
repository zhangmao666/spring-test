package com.example.springboottest.modules.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AiModelResponse {

    private Long id;

    private String provider;

    private String displayName;

    private String baseUrl;

    private String modelName;

    private Boolean enabled;

    private Boolean isDefault;

    private Boolean supportsDeepThinking;

    private Boolean supportsWebSearch;

    private String remark;

    private String maskedApiKey;

    private Boolean apiKeyConfigured;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
