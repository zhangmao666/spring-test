package com.example.springboottest.modules.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 转账请求DTO
 */
@Data
@Schema(description = "转账请求")
public class TransferRequest {
    
    @NotBlank(message = "支付渠道不能为空")
    @Pattern(regexp = "^(alipay|wechat)$", message = "支付渠道只能是alipay或wechat")
    @Schema(description = "支付渠道: alipay-支付宝, wechat-微信", example = "alipay")
    private String channel;
    
    @NotNull(message = "转账金额不能为空")
    @DecimalMin(value = "0.01", message = "转账金额最小0.01元")
    @DecimalMax(value = "50000.00", message = "转账金额最大50000元")
    @Schema(description = "转账金额(元)", example = "100.00")
    private BigDecimal amount;
    
    @NotBlank(message = "收款账户不能为空")
    @Schema(description = "收款账户(支付宝账号/微信openid)", example = "example@alipay.com")
    private String payeeAccount;
    
    @NotBlank(message = "收款人姓名不能为空")
    @Schema(description = "收款人真实姓名", example = "张三")
    private String payeeName;
    
    @Size(max = 200, message = "备注最长200字符")
    @Schema(description = "转账备注", example = "工资发放")
    private String remark;
}
