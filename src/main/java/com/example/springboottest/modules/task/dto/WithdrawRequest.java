package com.example.springboottest.modules.task.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 撤回请求DTO
 */
@Data
public class WithdrawRequest {
    @Size(max = 500, message = "撤回原因不能超过500字符")
    private String reason;
}
