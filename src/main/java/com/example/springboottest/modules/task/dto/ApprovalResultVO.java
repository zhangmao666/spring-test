package com.example.springboottest.modules.task.dto;

import lombok.Data;

/**
 * 审批结果VO
 */
@Data
public class ApprovalResultVO {
    private Long taskId;
    private String taskNo;
    private String status;
    private String statusText;
    private String message;
    private Boolean flowCompleted;
    private String nextNodeName;
}
