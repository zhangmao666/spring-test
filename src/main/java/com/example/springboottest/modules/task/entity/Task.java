package com.example.springboottest.modules.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tasks")
public class Task {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("task_no")
    private String taskNo;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("task_type")
    private String taskType;

    @TableField("priority")
    private Integer priority;

    @TableField("status")
    private String status;

    @TableField("current_node_id")
    private Long currentNodeId;

    @TableField("current_node_order")
    private Integer currentNodeOrder;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("creator_name")
    private String creatorName;

    @TableField("flow_id")
    private Long flowId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField("submitted_at")
    private LocalDateTime submittedAt;

    @TableField("completed_at")
    private LocalDateTime completedAt;
}
