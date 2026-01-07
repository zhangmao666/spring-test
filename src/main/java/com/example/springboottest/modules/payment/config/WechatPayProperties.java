package com.example.springboottest.modules.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment.wechat")
public class WechatPayProperties {
    
    /**
     * 商户号
     */
    private String merchantId;
    
    /**
     * 商户API私钥路径
     */
    private String privateKeyPath;
    
    /**
     * 商户API私钥内容 (与privateKeyPath二选一)
     */
    private String privateKey;
    
    /**
     * 商户证书序列号
     */
    private String merchantSerialNumber;
    
    /**
     * APIv3密钥
     */
    private String apiV3Key;
    
    /**
     * 应用ID (公众号/小程序/APP)
     */
    private String appId;
    
    /**
     * 异步通知地址
     */
    private String notifyUrl;
    
    /**
     * 服务地址
     * 正式环境: https://api.mch.weixin.qq.com
     */
    private String serviceUrl = "https://api.mch.weixin.qq.com";
}
