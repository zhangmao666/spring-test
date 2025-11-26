package com.example.springboottest.modules.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 驳回请求DTO
 */
@Data
public class RejectRequest {
    private Long taskId;

    @NotNull(message = "驳回目标节点不能为空")
    private Long rejectToNodeId;

    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 1000, message = "驳回原因不能超过1000字符")
    private String comment;
}
