package com.example.springboottest.modules.task.dto;

import com.example.springboottest.modules.task.enums.ApprovalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 审批节点请求DTO
 */
@Data
public class FlowNodeRequest {
    @NotBlank(message = "节点编码不能为空")
    private String nodeCode;

    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    @NotNull(message = "节点顺序不能为空")
    private Integer nodeOrder;

    @NotNull(message = "审批类型不能为空")
    private ApprovalType approvalType;

    @NotBlank(message = "审批人类型不能为空")
    private String approverType;

    private List<Long> approverIds;
    private List<String> approverRoles;
    private Integer timeoutHours;
}
