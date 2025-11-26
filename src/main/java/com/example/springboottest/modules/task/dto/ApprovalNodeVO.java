package com.example.springboottest.modules.task.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批节点VO
 */
@Data
public class ApprovalNodeVO {
    private Long id;
    private String nodeCode;
    private String nodeName;
    private Integer nodeOrder;
    private String approvalType;
    private String approvalTypeText;
    private List<String> approverNames;
    private String status;
    private LocalDateTime completedTime;
}
