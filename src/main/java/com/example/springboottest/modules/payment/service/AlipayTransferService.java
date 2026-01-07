package com.example.springboottest.modules.payment.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayFundTransUniTransferRequest;
import com.alipay.api.response.AlipayFundTransUniTransferResponse;
import com.example.springboottest.exception.BusinessException;
import com.example.springboottest.modules.payment.config.AlipayProperties;
import com.example.springboottest.modules.payment.dto.TransferRequest;
import com.example.springboottest.modules.payment.entity.TransferOrder;
import com.example.springboottest.modules.payment.enums.TransferStatus;
import com.example.springboottest.modules.payment.repository.TransferOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付宝转账服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(AlipayClient.class)
public class AlipayTransferService {
    
    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;
    private final TransferOrderRepository transferOrderRepository;
    
    /**
     * 发起支付宝转账
     */
    @Transactional
    public TransferOrder transfer(TransferRequest request, String outTradeNo, Long userId) {
        log.info("发起支付宝转账, outTradeNo: {}, amount: {}, payeeAccount: {}",
                outTradeNo, request.getAmount(), request.getPayeeAccount());
        
        // 创建转账订单
        TransferOrder order = new TransferOrder();
        order.setOutTradeNo(outTradeNo);
        order.setChannel("alipay");
        order.setAmount(request.getAmount());
        order.setPayeeAccount(request.getPayeeAccount());
        order.setPayeeName(request.getPayeeName());
        order.setRemark(request.getRemark());
        order.setStatus(TransferStatus.PROCESSING.getCode());
        order.setUserId(userId);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        transferOrderRepository.insert(order);
        
        try {
            // 构建转账请求
            AlipayFundTransUniTransferRequest transferRequest = new AlipayFundTransUniTransferRequest();
            
            // 金额转换为分
            BigDecimal amountInCents = request.getAmount();
            
            String bizContent = String.format("""
                {
                    "out_biz_no": "%s",
                    "trans_amount": "%s",
                    "product_code": "TRANS_ACCOUNT_NO_PWD",
                    "biz_scene": "DIRECT_TRANSFER",
                    "order_title": "%s",
                    "payee_info": {
                        "identity": "%s",
                        "identity_type": "ALIPAY_LOGON_ID",
                        "name": "%s"
                    },
                    "remark": "%s"
                }
                """,
                    outTradeNo,
                    amountInCents.toString(),
                    request.getRemark() != null ? request.getRemark() : "转账",
                    request.getPayeeAccount(),
                    request.getPayeeName(),
                    request.getRemark() != null ? request.getRemark() : "转账"
            );
            
            transferRequest.setBizContent(bizContent);
            
            // 执行转账
            AlipayFundTransUniTransferResponse response = alipayClient.execute(transferRequest);
            
            if (response.isSuccess()) {
                log.info("支付宝转账成功, outTradeNo: {}, tradeNo: {}", outTradeNo, response.getOrderId());
                order.setTradeNo(response.getOrderId());
                order.setStatus(TransferStatus.SUCCESS.getCode());
                order.setFinishTime(LocalDateTime.now());
            } else {
                log.error("支付宝转账失败, outTradeNo: {}, code: {}, msg: {}",
                        outTradeNo, response.getCode(), response.getMsg());
                order.setStatus(TransferStatus.FAILED.getCode());
                order.setFailReason(response.getSubMsg() != null ? response.getSubMsg() : response.getMsg());
            }
            
            order.setUpdateTime(LocalDateTime.now());
            transferOrderRepository.updateById(order);
            
        } catch (AlipayApiException e) {
            log.error("支付宝转账异常, outTradeNo: {}", outTradeNo, e);
            order.setStatus(TransferStatus.FAILED.getCode());
            order.setFailReason("系统异常: " + e.getErrMsg());
            order.setUpdateTime(LocalDateTime.now());
            transferOrderRepository.updateById(order);
            throw new BusinessException("支付宝转账失败: " + e.getErrMsg());
        }
        
        return order;
    }
    
    /**
     * 验证支付宝异步通知签名
     */
    public boolean verifyNotifySign(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );
        } catch (AlipayApiException e) {
            log.error("验证支付宝签名异常", e);
            return false;
        }
    }
    
    /**
     * 处理支付宝异步通知
     */
    @Transactional
    public void handleNotify(Map<String, String> params) {
        String outBizNo = params.get("out_biz_no");
        String status = params.get("status");
        
        log.info("处理支付宝转账通知, outBizNo: {}, status: {}", outBizNo, status);
        
        transferOrderRepository.findByOutTradeNo(outBizNo).ifPresent(order -> {
            if ("SUCCESS".equals(status)) {
                order.setStatus(TransferStatus.SUCCESS.getCode());
                order.setTradeNo(params.get("order_id"));
                order.setFinishTime(LocalDateTime.now());
            } else if ("FAIL".equals(status)) {
                order.setStatus(TransferStatus.FAILED.getCode());
                order.setFailReason(params.get("fail_reason"));
            }
            order.setUpdateTime(LocalDateTime.now());
            transferOrderRepository.updateById(order);
        });
    }
}
