package com.example.springboottest.modules.task.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 转交请求DTO
 */
@Data
public class TransferRequest {
    private Long taskId;

    @NotNull(message = "转交目标用户不能为空")
    private Long transferToUserId;

    @Size(max = 1000, message = "转交说明不能超过1000字符")
    private String comment;
}
