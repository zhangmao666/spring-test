package com.example.springboottest.modules.ai.skill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("ai_skill_execution_log")
public class AiSkillExecutionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("conversation_id")
    private String conversationId;

    @TableField("skill_id")
    private Long skillId;

    @TableField("tool_name")
    private String toolName;

    @TableField("arguments_payload")
    private String argumentsPayload;

    private String status;

    @TableField("output_summary")
    private String outputSummary;

    @TableField("error_message")
    private String errorMessage;

    @TableField("duration_ms")
    private Long durationMs;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
