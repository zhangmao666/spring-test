package com.example.springboottest.modules.payment.service;

import com.example.springboottest.exception.BusinessException;
import com.example.springboottest.modules.payment.config.WechatPayProperties;
import com.example.springboottest.modules.payment.dto.TransferRequest;
import com.example.springboottest.modules.payment.entity.TransferOrder;
import com.example.springboottest.modules.payment.enums.TransferStatus;
import com.example.springboottest.modules.payment.repository.TransferOrderRepository;
import com.wechat.pay.java.service.transferbatch.TransferBatchService;
import com.wechat.pay.java.service.transferbatch.model.GetTransferBatchByOutNoRequest;
import com.wechat.pay.java.service.transferbatch.model.InitiateBatchTransferRequest;
import com.wechat.pay.java.service.transferbatch.model.InitiateBatchTransferResponse;
import com.wechat.pay.java.service.transferbatch.model.TransferBatchEntity;
import com.wechat.pay.java.service.transferbatch.model.TransferDetailCompact;
import com.wechat.pay.java.service.transferbatch.model.TransferDetailInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 微信支付转账服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(TransferBatchService.class)
public class WechatTransferService {
    
    private final TransferBatchService transferBatchService;
    private final WechatPayProperties wechatPayProperties;
    private final TransferOrderRepository transferOrderRepository;
    
    /**
     * 发起微信转账
     */
    @Transactional
    public TransferOrder transfer(TransferRequest request, String outTradeNo, Long userId) {
        log.info("发起微信转账, outTradeNo: {}, amount: {}, payeeAccount: {}",
                outTradeNo, request.getAmount(), request.getPayeeAccount());
        
        // 创建转账订单
        TransferOrder order = new TransferOrder();
        order.setOutTradeNo(outTradeNo);
        order.setChannel("wechat");
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
            // 构建转账明细
            TransferDetailInput transferDetail = new TransferDetailInput();
            transferDetail.setOutDetailNo(outTradeNo);
            transferDetail.setTransferAmount(request.getAmount().multiply(new java.math.BigDecimal("100")).longValue());
            transferDetail.setTransferRemark(request.getRemark() != null ? request.getRemark() : "转账");
            transferDetail.setOpenid(request.getPayeeAccount());
            transferDetail.setUserName(request.getPayeeName());
            
            // 构建转账批次请求
            InitiateBatchTransferRequest batchRequest = new InitiateBatchTransferRequest();
            batchRequest.setAppid(wechatPayProperties.getAppId());
            batchRequest.setOutBatchNo(outTradeNo);
            batchRequest.setBatchName(request.getRemark() != null ? request.getRemark() : "转账");
            batchRequest.setBatchRemark(request.getRemark() != null ? request.getRemark() : "转账");
            batchRequest.setTotalAmount(request.getAmount().multiply(new java.math.BigDecimal("100")).longValue());
            batchRequest.setTotalNum(1);
            batchRequest.setTransferDetailList(Collections.singletonList(transferDetail));
            
            // 执行转账
            InitiateBatchTransferResponse response = transferBatchService.initiateBatchTransfer(batchRequest);
            
            log.info("微信转账请求成功, outTradeNo: {}, batchId: {}", outTradeNo, response.getBatchId());
            order.setTradeNo(response.getBatchId());
            order.setStatus(TransferStatus.PROCESSING.getCode());
            order.setUpdateTime(LocalDateTime.now());
            transferOrderRepository.updateById(order);
            
        } catch (Exception e) {
            log.error("微信转账异常, outTradeNo: {}", outTradeNo, e);
            order.setStatus(TransferStatus.FAILED.getCode());
            order.setFailReason("系统异常: " + e.getMessage());
            order.setUpdateTime(LocalDateTime.now());
            transferOrderRepository.updateById(order);
            throw new BusinessException("微信转账失败: " + e.getMessage());
        }
        
        return order;
    }
    
    /**
     * 查询微信转账批次状态
     */
    public void queryAndUpdateStatus(TransferOrder order) {
        if (order.getTradeNo() == null) {
            return;
        }
        
        try {
            GetTransferBatchByOutNoRequest request = new GetTransferBatchByOutNoRequest();
            request.setOutBatchNo(order.getOutTradeNo());
            request.setNeedQueryDetail(true);
            request.setOffset(0);
            request.setLimit(20);
            request.setDetailStatus("ALL");
            
            TransferBatchEntity response = transferBatchService.getTransferBatchByOutNo(request);
            
            String batchStatus = response.getTransferBatch().getBatchStatus();
            log.info("查询微信转账状态, outTradeNo: {}, status: {}", order.getOutTradeNo(), batchStatus);
            
            switch (batchStatus) {
                case "FINISHED":
                    // 检查明细状态
                    if (response.getTransferDetailList() != null && !response.getTransferDetailList().isEmpty()) {
                        TransferDetailCompact detail = response.getTransferDetailList().get(0);
                        if ("SUCCESS".equals(detail.getDetailStatus())) {
                            order.setStatus(TransferStatus.SUCCESS.getCode());
                            order.setFinishTime(LocalDateTime.now());
                        } else if ("FAIL".equals(detail.getDetailStatus())) {
                            order.setStatus(TransferStatus.FAILED.getCode());
                            order.setFailReason("转账失败");
                        }
                    }
                    break;
                case "CLOSED":
                    order.setStatus(TransferStatus.CLOSED.getCode());
                    break;
                default:
                    // ACCEPTED, PROCESSING 保持处理中状态
                    break;
            }
            
            order.setUpdateTime(LocalDateTime.now());
            transferOrderRepository.updateById(order);
            
        } catch (Exception e) {
            log.error("查询微信转账状态异常, outTradeNo: {}", order.getOutTradeNo(), e);
        }
    }
}
