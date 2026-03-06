package com.example.springboottest.modules.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 高分作文生成请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EssayGenerateRequest {

    @NotBlank(message = "作文题目不能为空")
    @Size(max = 120, message = "作文题目长度不能超过120")
    private String topic;

    @NotBlank(message = "年级不能为空")
    @Size(max = 50, message = "年级长度不能超过50")
    private String gradeLevel;

    @NotBlank(message = "作文体裁不能为空")
    @Size(max = 50, message = "作文体裁长度不能超过50")
    private String genre;

    @Min(value = 300, message = "目标字数不能少于300")
    @Max(value = 2000, message = "目标字数不能超过2000")
    @Builder.Default
    private Integer expectedWordCount = 800;

    @Size(max = 600, message = "补充要求长度不能超过600")
    private String requirements;

    @Builder.Default
    private Boolean useDeepThinking = true;

    @Builder.Default
    private Boolean useWebSearch = false;

    @Min(value = 100, message = "maxTokens 不能小于100")
    @Max(value = 4000, message = "maxTokens 不能超过4000")
    @Builder.Default
    private Integer maxTokens = 1800;

    @DecimalMin(value = "0.0", message = "temperature 不能小于0")
    @DecimalMax(value = "2.0", message = "temperature 不能大于2")
    @Builder.Default
    private Double temperature = 0.7;

    @Size(max = 64, message = "会话ID长度不能超过64")
    private String conversationId;
}
