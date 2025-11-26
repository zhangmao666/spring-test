package com.example.springboottest.modules.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审批节点配置实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_approval_nodes")
public class TaskApprovalNode {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("flow_id")
    private Long flowId;

    @TableField("node_code")
    private String nodeCode;

    @TableField("node_name")
    private String nodeName;

    @TableField("node_order")
    private Integer nodeOrder;

    @TableField("approval_type")
    private String approvalType;

    @TableField("approver_type")
    private String approverType;

    @TableField("approver_ids")
    private String approverIds;

    @TableField("approver_roles")
    private String approverRoles;

    @TableField("auto_pass")
    private Integer autoPass;

    @TableField("timeout_hours")
    private Integer timeoutHours;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
