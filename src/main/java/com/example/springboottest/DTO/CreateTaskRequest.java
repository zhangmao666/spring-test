package com.example.springboottest.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 创建任务请求DTO
 */
@Data
public class CreateTaskRequest {

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    @Size(max = 5000, message = "内容长度不能超过5000字符")
    private String content;

    private String taskType;

    @Min(value = 1, message = "优先级必须在1-4之间")
    @Max(value = 4, message = "优先级必须在1-4之间")
    private Integer priority = 1;

    @NotNull(message = "审批流ID不能为空")
    private Long flowId;
}
