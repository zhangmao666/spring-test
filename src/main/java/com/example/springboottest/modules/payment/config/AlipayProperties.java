package com.example.springboottest.modules.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment.alipay")
public class AlipayProperties {
    
    /**
     * 应用ID
     */
    private String appId;
    
    /**
     * 商户私钥
     */
    private String privateKey;
    
    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;
    
    /**
     * 网关地址
     * 正式环境: https://openapi.alipay.com/gateway.do
     * 沙箱环境: https://openapi-sandbox.dl.alipaydev.com/gateway.do
     */
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    
    /**
     * 编码格式
     */
    private String charset = "UTF-8";
    
    /**
     * 签名类型
     */
    private String signType = "RSA2";
    
    /**
     * 数据格式
     */
    private String format = "json";
    
    /**
     * 异步通知地址
     */
    private String notifyUrl;
    
    /**
     * 同步跳转地址
     */
    private String returnUrl;
}
