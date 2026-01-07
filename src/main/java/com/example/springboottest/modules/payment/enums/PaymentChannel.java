package com.example.springboottest.modules.payment.enums;

import lombok.Getter;

/**
 * 支付渠道枚举
 */
@Getter
public enum PaymentChannel {
    
    ALIPAY("alipay", "支付宝"),
    WECHAT("wechat", "微信支付");
    
    private final String code;
    private final String description;
    
    PaymentChannel(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public static PaymentChannel fromCode(String code) {
        for (PaymentChannel channel : values()) {
            if (channel.getCode().equalsIgnoreCase(code)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("未知支付渠道: " + code);
    }
}
