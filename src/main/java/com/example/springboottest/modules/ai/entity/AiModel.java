package com.example.springboottest.modules.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.springboottest.enums.AIEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @Author: zm
 * @Created: 2025/12/2 上午10:37
 * @Description: ai模型
 */
@Data
@Accessors(chain = true)
@TableName("ai_model")
public class AiModel {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "AI模型策略")
    private AIEnum aiEnum;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "更新人")
    private Long updateBy;
}