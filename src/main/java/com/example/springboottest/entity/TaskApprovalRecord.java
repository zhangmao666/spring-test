package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审批记录实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_approval_records")
public class TaskApprovalRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    private Long taskId;

    @TableField("node_id")
    private Long nodeId;

    @TableField("node_name")
    private String nodeName;

    @TableField("node_order")
    private Integer nodeOrder;

    @TableField("approver_id")
    private Long approverId;

    @TableField("approver_name")
    private String approverName;

    @TableField("action")
    private String action;

    @TableField("result")
    private String result;

    @TableField("comment")
    private String comment;

    @TableField("reject_to_node_id")
    private Long rejectToNodeId;

    @TableField("transfer_to_user_id")
    private Long transferToUserId;

    @TableField("transfer_to_user_name")
    private String transferToUserName;

    @TableField("approval_time")
    private LocalDateTime approvalTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
