package com.example.springboottest.controller;

import com.example.springboottest.DTO.ApiResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 模拟Token接口
 * 用于测试，返回模拟的token数据
 */
@Slf4j
@RestController
@RequestMapping("/mock")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class MockTokenController {

    /**
     * 模拟OAuth2 Token获取接口
     * 
     * @param request 请求参数
     * @return 模拟的token响应
     */
    @PostMapping("/token")
    public ApiResponse<MockTokenResponse> getMockToken(@RequestBody(required = false) MockTokenRequest request) {
        log.info("接收到模拟token请求: {}", request);

        if(request.getClientId() == null || request.getClientId().isEmpty()) {
            log.warn("客户端ID不能为空");
            return ApiResponse.error("客户端ID不能为空");
        }

        if (request.getClientSecret() == null || request.getClientSecret().isEmpty()) {
            log.warn("客户端密钥不能为空");
            return ApiResponse.error("客户端密钥不能为空");
        }

        if (!request.getClientId().equals("client_test_demo_001") || !request.getClientSecret().equals("123456")) {
            log.warn("客户端认证失败: clientId={}, clientSecret={}", request.getClientId(), request.getClientSecret());
            return ApiResponse.error("客户端ID或密钥错误");
        }
        
        // 生成模拟token
        String accessToken = generateMockToken();
        
        MockTokenResponse response = new MockTokenResponse();
        response.setAccessToken(accessToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(7200); // 2小时
        response.setScope("read write");
        response.setIssuedAt(LocalDateTime.now());
        
        log.info("返回模拟token: {}", accessToken.substring(0, 20) + "...");
        
        return ApiResponse.success("模拟token获取成功", response);
    }
    
    /**
     * 简化版 - GET方式获取模拟token
     */
    @GetMapping("/token")
    public ApiResponse<MockTokenResponse> getMockTokenSimple(
            @RequestParam(required = false, defaultValue = "test_client") String clientId) {
        
        log.info("GET方式获取模拟token, clientId: {}", clientId);
        
        String accessToken = generateMockToken();
        
        MockTokenResponse response = new MockTokenResponse();
        response.setAccessToken(accessToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(7200);
        response.setScope("read write");
        response.setIssuedAt(LocalDateTime.now());
        response.setClientId(clientId);
        
        return ApiResponse.success("模拟token获取成功", response);
    }
    
    /**
     * 模拟标准OAuth2格式的token响应
     */
    @PostMapping("/oauth2/token")
    public Map<String, Object> getMockOAuth2Token(
            @RequestParam(required = false) String grantType,
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) String clientSecret) {
        
        log.info("模拟OAuth2 token请求 - grant_type: {}, client_id: {}", grantType, clientId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", generateMockToken());
        response.put("token_type", "Bearer");
        response.put("expires_in", 7200);
        response.put("scope", "read write");
        response.put("issued_at", System.currentTimeMillis() / 1000);
        
        return response;
    }
    
    /**
     * 验证模拟token
     */
    @PostMapping("/verify")
    public ApiResponse<TokenVerifyResponse> verifyMockToken(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        log.info("验证模拟token: {}", authorization);
        
        TokenVerifyResponse response = new TokenVerifyResponse();
        
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            response.setValid(true);
            response.setToken(token);
            response.setMessage("Token有效");
            response.setClientId("test_client");
            response.setScopes("read,write");
            response.setExpiresAt(LocalDateTime.now().plusHours(2));
        } else {
            response.setValid(false);
            response.setMessage("Token无效或缺失");
        }
        
        return ApiResponse.success("验证完成", response);
    }
    
    /**
     * 获取token信息
     */
    @GetMapping("/token-info")
    public ApiResponse<Map<String, Object>> getTokenInfo(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Map<String, Object> info = new HashMap<>();
        
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            info.put("token", token);
            info.put("token_length", token.length());
            info.put("token_prefix", token.substring(0, Math.min(20, token.length())) + "...");
            info.put("valid", true);
            info.put("client_id", "test_client");
            info.put("scopes", new String[]{"read", "write"});
            info.put("issued_at", LocalDateTime.now());
            info.put("expires_at", LocalDateTime.now().plusHours(2));
        } else {
            info.put("valid", false);
            info.put("message", "未提供Authorization header");
        }
        
        return ApiResponse.success("Token信息", info);
    }
    
    /**
     * 生成模拟的JWT格式token
     */
    private String generateMockToken() {
        // 模拟JWT格式：header.payload.signature
        String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String payload = base64UrlEncode(String.format(
            "{\"sub\":\"test_client\",\"scope\":\"read write\",\"iat\":%d,\"exp\":%d}",
            System.currentTimeMillis() / 1000,
            System.currentTimeMillis() / 1000 + 7200
        ));
        String signature = base64UrlEncode(UUID.randomUUID().toString());
        
        return header + "." + payload + "." + signature;
    }
    
    /**
     * 简单的Base64URL编码
     */
    private String base64UrlEncode(String input) {
        return java.util.Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(input.getBytes());
    }
    
    /**
     * 模拟Token请求
     */
    @Data
    public static class MockTokenRequest {

        @JsonProperty("grant_type")
        private String grantType;
        @JsonProperty("client_id")
        private String clientId;
        @JsonProperty("client_secret")
        private String clientSecret;
        @JsonProperty("scope")
        private String scope;
    }
    
    /**
     * 模拟Token响应
     */
    @Data
    public static class MockTokenResponse {
        private String accessToken;
        private String tokenType;
        private Integer expiresIn;
        private String scope;
        private String clientId;
        private LocalDateTime issuedAt;
    }
    
    /**
     * Token验证响应
     */
    @Data
    public static class TokenVerifyResponse {
        private Boolean valid;
        private String token;
        private String message;
        private String clientId;
        private String scopes;
        private LocalDateTime expiresAt;
    }
}

