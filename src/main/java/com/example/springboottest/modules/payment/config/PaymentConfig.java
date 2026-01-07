package com.example.springboottest.modules.payment.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.transferbatch.TransferBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PaymentConfig {
    
    private final AlipayProperties alipayProperties;
    private final WechatPayProperties wechatPayProperties;
    
    /**
     * 支付宝客户端
     */
    @Bean
    @ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${payment.alipay.app-id:}')")
    public AlipayClient alipayClient() {
        log.info("初始化支付宝客户端, appId: {}", alipayProperties.getAppId());
        return new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                alipayProperties.getFormat(),
                alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getSignType()
        );
    }
    
    /**
     * 微信支付配置
     */
    @Bean
    @ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${payment.wechat.merchant-id:}') && !T(org.springframework.util.StringUtils).isEmpty('${payment.wechat.private-key:}')")
    public Config wechatPayConfig() {
        log.info("初始化微信支付配置, merchantId: {}", wechatPayProperties.getMerchantId());
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(wechatPayProperties.getMerchantId())
                .privateKey(wechatPayProperties.getPrivateKey())
                .merchantSerialNumber(wechatPayProperties.getMerchantSerialNumber())
                .apiV3Key(wechatPayProperties.getApiV3Key())
                .build();
    }
    
    /**
     * 微信支付转账服务
     */
    @Bean
    @ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${payment.wechat.merchant-id:}') && !T(org.springframework.util.StringUtils).isEmpty('${payment.wechat.private-key:}')")
    public TransferBatchService transferBatchService(Config wechatPayConfig) {
        log.info("初始化微信支付转账服务");
        return new TransferBatchService.Builder().config(wechatPayConfig).build();
    }
}
