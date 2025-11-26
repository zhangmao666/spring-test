package com.example.springboottest.modules.task.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

/**
 * 更新审批流请求DTO
 */
@Data
public class UpdateFlowRequest {
    private String flowName;
    private String description;

    @NotEmpty(message = "审批节点不能为空")
    @Valid
    private List<FlowNodeRequest> nodes;
}
