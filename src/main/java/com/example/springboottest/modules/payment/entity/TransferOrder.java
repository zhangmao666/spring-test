package com.example.springboottest.modules.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 转账订单实体
 */
@Data
@TableName("transfer_order")
public class TransferOrder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 商户订单号 (唯一)
     */
    private String outTradeNo;
    
    /**
     * 第三方交易号
     */
    private String tradeNo;
    
    /**
     * 支付渠道: alipay, wechat
     */
    private String channel;
    
    /**
     * 转账金额(元)
     */
    private BigDecimal amount;
    
    /**
     * 收款账户 (支付宝账号/微信openid)
     */
    private String payeeAccount;
    
    /**
     * 收款人姓名
     */
    private String payeeName;
    
    /**
     * 转账备注
     */
    private String remark;
    
    /**
     * 转账状态: 0-待处理, 1-处理中, 2-成功, 3-失败, 4-已关闭
     */
    private Integer status;
    
    /**
     * 失败原因
     */
    private String failReason;
    
    /**
     * 发起用户ID
     */
    private Long userId;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
}
