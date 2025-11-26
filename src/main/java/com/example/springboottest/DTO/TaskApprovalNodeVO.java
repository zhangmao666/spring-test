package com.example.springboottest.DTO;

import lombok.Data;

import java.util.List;

/**
 * 审批节点配置VO
 */
@Data
public class TaskApprovalNodeVO {

    private Long id;
    private String nodeCode;
    private String nodeName;
    private Integer nodeOrder;
    private String approvalType;
    private String approverType;
    private List<String> approverNames;
    private List<String> approverRoleNames;
    private Integer timeoutHours;
}
