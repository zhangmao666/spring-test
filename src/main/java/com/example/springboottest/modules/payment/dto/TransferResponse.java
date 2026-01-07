package com.example.springboottest.modules.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 转账响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "转账响应")
public class TransferResponse {
    
    @Schema(description = "订单ID")
    private Long orderId;
    
    @Schema(description = "商户订单号")
    private String outTradeNo;
    
    @Schema(description = "第三方交易号")
    private String tradeNo;
    
    @Schema(description = "支付渠道")
    private String channel;
    
    @Schema(description = "转账金额")
    private BigDecimal amount;
    
    @Schema(description = "收款账户")
    private String payeeAccount;
    
    @Schema(description = "收款人姓名")
    private String payeeName;
    
    @Schema(description = "转账状态: 0-待处理, 1-处理中, 2-成功, 3-失败")
    private Integer status;
    
    @Schema(description = "状态描述")
    private String statusDesc;
    
    @Schema(description = "失败原因")
    private String failReason;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "完成时间")
    private LocalDateTime finishTime;
}
