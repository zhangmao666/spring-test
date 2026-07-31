package com.example.springboottest.entity.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过2000个字符")
    private String message;

    private String provider;

    private String model;

    private Long modelId;

    private Double temperature;

    private Integer maxTokens;

    private String conversationId;

    private Boolean useWebSearch;

    private Boolean useDeepThinking;

    private Boolean useAgent;

    private String agentMode;

    private String permissionMode;

    private String resumeRunId;

    private java.util.List<Long> skillIds;
}
