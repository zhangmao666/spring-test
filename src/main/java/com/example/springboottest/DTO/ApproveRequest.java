package com.example.springboottest.DTO;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 审批通过请求DTO
 */
@Data
public class ApproveRequest {

    private Long taskId;

    @Size(max = 1000, message = "审批意见不能超过1000字符")
    private String comment;
}
