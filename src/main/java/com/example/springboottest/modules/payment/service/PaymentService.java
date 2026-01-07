package com.example.springboottest.modules.payment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboottest.modules.payment.dto.TransferQueryRequest;
import com.example.springboottest.modules.payment.dto.TransferRequest;
import com.example.springboottest.modules.payment.dto.TransferResponse;

/**
 * 支付服务接口
 */
public interface PaymentService {
    
    /**
     * 发起转账
     *
     * @param request 转账请求
     * @param userId  用户ID
     * @return 转账响应
     */
    TransferResponse transfer(TransferRequest request, Long userId);
    
    /**
     * 查询转账订单
     *
     * @param outTradeNo 商户订单号
     * @return 转账响应
     */
    TransferResponse queryByOutTradeNo(String outTradeNo);
    
    /**
     * 分页查询转账订单
     *
     * @param request 查询请求
     * @param userId  用户ID
     * @return 分页结果
     */
    IPage<TransferResponse> queryTransferOrders(TransferQueryRequest request, Long userId);
    
    /**
     * 处理支付宝异步通知
     *
     * @param params 通知参数
     * @return 处理结果
     */
    String handleAlipayNotify(java.util.Map<String, String> params);
    
    /**
     * 处理微信支付异步通知
     *
     * @param requestBody 请求体
     * @param signature   签名
     * @param timestamp   时间戳
     * @param nonce       随机串
     * @param serial      证书序列号
     * @return 处理结果
     */
    String handleWechatNotify(String requestBody, String signature, String timestamp, String nonce, String serial);
}
