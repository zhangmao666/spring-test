package com.example.springboottest.modules.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * OpenAI-compatible model registry entity.
 */
@Data
@Accessors(chain = true)
@TableName("ai_model")
public class AiModel {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Primary key")
    private Long id;

    @TableField("ai_enum")
    @Schema(description = "Provider or channel key")
    private String provider;

    @TableField("display_name")
    @Schema(description = "Display name")
    private String displayName;

    @TableField("base_url")
    @Schema(description = "OpenAI-compatible base URL")
    private String baseUrl;

    @TableField("api_key")
    @Schema(description = "API key")
    private String apiKey;

    @TableField("model_name")
    @Schema(description = "Model name")
    private String modelName;

    @Schema(description = "Whether this model is enabled")
    private Boolean enabled;

    @TableField("is_default")
    @Schema(description = "Whether this model is the default model")
    private Boolean isDefault;

    @TableField("supports_deep_thinking")
    @Schema(description = "Whether deep thinking is supported")
    private Boolean supportsDeepThinking;

    @TableField("supports_web_search")
    @Schema(description = "Whether web search is supported")
    private Boolean supportsWebSearch;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Create time")
    private LocalDateTime createTime;

    @Schema(description = "Create by")
    private Long createBy;

    @Schema(description = "Update time")
    private LocalDateTime updateTime;

    @Schema(description = "Update by")
    private Long updateBy;
}
