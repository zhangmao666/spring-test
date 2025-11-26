package com.example.springboottest.modules.task.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批流VO
 */
@Data
public class TaskApprovalFlowVO {
    private Long id;
    private String flowCode;
    private String flowName;
    private String description;
    private String taskType;
    private Integer status;
    private Integer version;
    private LocalDateTime createdAt;
}
