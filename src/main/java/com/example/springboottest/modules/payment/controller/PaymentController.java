package com.example.springboottest.modules.payment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.payment.dto.TransferQueryRequest;
import com.example.springboottest.modules.payment.dto.TransferRequest;
import com.example.springboottest.modules.payment.dto.TransferResponse;
import com.example.springboottest.modules.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付转账控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "支付转账", description = "支付宝/微信转账相关接口")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/transfer")
    @Operation(summary = "发起转账", description = "发起支付宝或微信转账")
    public ApiResponse<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        // 从认证信息中获取用户ID，这里简化处理
        Long userId = getUserId(userDetails);
        
        TransferResponse response = paymentService.transfer(request, userId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/query/{outTradeNo}")
    @Operation(summary = "查询转账订单", description = "根据商户订单号查询转账订单")
    public ApiResponse<TransferResponse> queryByOutTradeNo(
            @PathVariable String outTradeNo) {
        
        TransferResponse response = paymentService.queryByOutTradeNo(outTradeNo);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/orders")
    @Operation(summary = "查询转账订单列表", description = "分页查询当前用户的转账订单")
    public ApiResponse<IPage<TransferResponse>> queryOrders(
            TransferQueryRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        IPage<TransferResponse> page = paymentService.queryTransferOrders(request, userId);
        return ApiResponse.success(page);
    }
    
    @PostMapping("/notify/alipay")
    @Operation(summary = "支付宝异步通知", description = "接收支付宝转账结果异步通知")
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            params.put(name, request.getParameter(name));
        }
        
        return paymentService.handleAlipayNotify(params);
    }
    
    @PostMapping("/notify/wechat")
    @Operation(summary = "微信支付异步通知", description = "接收微信支付转账结果异步通知")
    public String wechatNotify(
            @RequestBody String requestBody,
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
            @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
            @RequestHeader(value = "Wechatpay-Serial", required = false) String serial) {
        
        return paymentService.handleWechatNotify(requestBody, signature, timestamp, nonce, serial);
    }
    
    /**
     * 从UserDetails获取用户ID
     */
    private Long getUserId(UserDetails userDetails) {
        if (userDetails == null) {
            return 1L; // 默认用户ID，实际应用中应该抛出未认证异常
        }
        // 这里简化处理，实际应该从自定义UserDetails中获取
        try {
            return Long.parseLong(userDetails.getUsername());
        } catch (NumberFormatException e) {
            return 1L;
        }
    }
}
