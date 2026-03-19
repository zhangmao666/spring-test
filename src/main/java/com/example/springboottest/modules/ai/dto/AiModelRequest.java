package com.example.springboottest.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiModelRequest {

    private Long id;

    @NotBlank(message = "渠道标识不能为空")
    @Size(max = 50, message = "渠道标识不能超过50个字符")
    private String provider;

    @NotBlank(message = "展示名称不能为空")
    @Size(max = 100, message = "展示名称不能超过100个字符")
    private String displayName;

    @NotBlank(message = "Base URL不能为空")
    @Size(max = 255, message = "Base URL不能超过255个字符")
    private String baseUrl;

    @Size(max = 255, message = "API Key不能超过255个字符")
    private String apiKey;

    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称不能超过100个字符")
    private String modelName;

    private Boolean enabled = true;

    private Boolean isDefault = false;

    private Boolean supportsDeepThinking = false;

    private Boolean supportsWebSearch = false;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
