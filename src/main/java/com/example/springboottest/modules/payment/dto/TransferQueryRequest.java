package com.example.springboottest.modules.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 转账查询请求DTO
 */
@Data
@Schema(description = "转账查询请求")
public class TransferQueryRequest {
    
    @Schema(description = "商户订单号")
    private String outTradeNo;
    
    @Schema(description = "支付渠道: alipay, wechat")
    private String channel;
    
    @Schema(description = "转账状态: 0-待处理, 1-处理中, 2-成功, 3-失败")
    private Integer status;
    
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
