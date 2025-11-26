package com.example.springboottest.DTO;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批记录VO
 */
@Data
public class ApprovalRecordVO {

    private Long id;
    private String nodeName;
    private Integer nodeOrder;
    private String approverName;
    private String action;
    private String actionText;
    private String result;
    private String resultText;
    private String comment;
    private String rejectToNodeName;
    private String transferToUserName;
    private LocalDateTime approvalTime;
}
