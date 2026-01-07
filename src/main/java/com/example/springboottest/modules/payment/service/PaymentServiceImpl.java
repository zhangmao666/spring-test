package com.example.springboottest.modules.payment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.exception.BusinessException;
import com.example.springboottest.modules.payment.dto.TransferQueryRequest;
import com.example.springboottest.modules.payment.dto.TransferRequest;
import com.example.springboottest.modules.payment.dto.TransferResponse;
import com.example.springboottest.modules.payment.entity.TransferOrder;
import com.example.springboottest.modules.payment.enums.PaymentChannel;
import com.example.springboottest.modules.payment.enums.TransferStatus;
import com.example.springboottest.modules.payment.repository.TransferOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * 支付服务实现类
 */
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {
    
    private final TransferOrderRepository transferOrderRepository;
    
    @Autowired(required = false)
    private AlipayTransferService alipayTransferService;
    
    @Autowired(required = false)
    private WechatTransferService wechatTransferService;
    
    public PaymentServiceImpl(TransferOrderRepository transferOrderRepository) {
        this.transferOrderRepository = transferOrderRepository;
    }
    
    @Override
    public TransferResponse transfer(TransferRequest request, Long userId) {
        String outTradeNo = generateOutTradeNo();
        PaymentChannel channel = PaymentChannel.fromCode(request.getChannel());
        
        log.info("发起转账, channel: {}, outTradeNo: {}, amount: {}, userId: {}",
                channel, outTradeNo, request.getAmount(), userId);
        
        TransferOrder order;
        
        switch (channel) {
            case ALIPAY:
                if (alipayTransferService == null) {
                    throw new BusinessException("支付宝转账服务未配置");
                }
                order = alipayTransferService.transfer(request, outTradeNo, userId);
                break;
            case WECHAT:
                if (wechatTransferService == null) {
                    throw new BusinessException("微信转账服务未配置");
                }
                order = wechatTransferService.transfer(request, outTradeNo, userId);
                break;
            default:
                throw new BusinessException("不支持的支付渠道: " + channel);
        }
        
        return convertToResponse(order);
    }
    
    @Override
    public TransferResponse queryByOutTradeNo(String outTradeNo) {
        TransferOrder order = transferOrderRepository.findByOutTradeNo(outTradeNo)
                .orElseThrow(() -> new BusinessException("转账订单不存在"));
        
        // 如果是处理中状态，尝试查询最新状态
        if (order.getStatus() == TransferStatus.PROCESSING.getCode()) {
            if ("wechat".equals(order.getChannel()) && wechatTransferService != null) {
                wechatTransferService.queryAndUpdateStatus(order);
                order = transferOrderRepository.selectById(order.getId());
            }
        }
        
        return convertToResponse(order);
    }
    
    @Override
    public IPage<TransferResponse> queryTransferOrders(TransferQueryRequest request, Long userId) {
        Page<TransferOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<TransferOrder> orderPage = transferOrderRepository.findByUserIdWithCondition(
                page, userId, request.getChannel(), request.getStatus());
        
        return orderPage.convert(this::convertToResponse);
    }
    
    @Override
    public String handleAlipayNotify(Map<String, String> params) {
        log.info("收到支付宝异步通知: {}", params);
        
        if (alipayTransferService == null) {
            log.error("支付宝转账服务未配置");
            return "failure";
        }
        
        // 验证签名
        if (!alipayTransferService.verifyNotifySign(params)) {
            log.error("支付宝签名验证失败");
            return "failure";
        }
        
        // 处理通知
        alipayTransferService.handleNotify(params);
        
        return "success";
    }
    
    @Override
    public String handleWechatNotify(String requestBody, String signature, String timestamp, String nonce, String serial) {
        log.info("收到微信支付异步通知");
        // 微信支付V3使用证书自动验签，这里简化处理
        // 实际生产环境需要完整的验签逻辑
        return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
    }
    
    /**
     * 生成商户订单号
     */
    private String generateOutTradeNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "TF" + timestamp + uuid;
    }
    
    /**
     * 转换为响应DTO
     */
    private TransferResponse convertToResponse(TransferOrder order) {
        return TransferResponse.builder()
                .orderId(order.getId())
                .outTradeNo(order.getOutTradeNo())
                .tradeNo(order.getTradeNo())
                .channel(order.getChannel())
                .amount(order.getAmount())
                .payeeAccount(order.getPayeeAccount())
                .payeeName(order.getPayeeName())
                .status(order.getStatus())
                .statusDesc(TransferStatus.fromCode(order.getStatus()).getDescription())
                .failReason(order.getFailReason())
                .createTime(order.getCreateTime())
                .finishTime(order.getFinishTime())
                .build();
    }
}
