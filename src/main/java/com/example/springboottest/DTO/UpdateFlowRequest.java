package com.example.springboottest.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 更新审批流请求DTO
 */
@Data
public class UpdateFlowRequest {

    @NotBlank(message = "审批流名称不能为空")
    private String flowName;

    private String description;

    private String taskType;

    @NotEmpty(message = "审批节点不能为空")
    @Valid
    private List<FlowNodeRequest> nodes;
}
