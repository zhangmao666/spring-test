package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审批流定义实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_approval_flows")
public class TaskApprovalFlow {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("flow_code")
    private String flowCode;

    @TableField("flow_name")
    private String flowName;

    @TableField("description")
    private String description;

    @TableField("task_type")
    private String taskType;

    @TableField("status")
    private Integer status;

    @TableField("version")
    private Integer version;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
