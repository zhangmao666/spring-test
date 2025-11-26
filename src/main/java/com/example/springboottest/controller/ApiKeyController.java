package com.example.springboottest.controller;

import com.example.springboottest.DTO.ApiResponse;
import com.example.springboottest.entity.ApiClient;
import com.example.springboottest.service.ApiKeyService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API Key管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    /**
     * 创建API客户端
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> createClient(@RequestBody CreateClientRequest request) {
        log.info("创建API客户端: clientName={}", request.getClientName());
        
        ApiClient client = apiKeyService.createClient(
            request.getClientName(),
            request.getScopes() != null ? request.getScopes() : "read,write",
            request.getDescription()
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("clientId", client.getClientId());
        result.put("clientName", client.getClientName());
        result.put("apiKey", client.getApiKey());
        result.put("scopes", client.getScopes());
        result.put("status", client.getStatus());
        result.put("message", "请妥善保管API Key，它不会再次显示！");
        
        return ApiResponse.success("客户端创建成功", result);
    }

    /**
     * 重新生成API Key
     */
    @PostMapping("/{clientId}/regenerate-key")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, String>> regenerateApiKey(@PathVariable String clientId) {
        log.info("重新生成API Key: clientId={}", clientId);
        
        ApiClient client = apiKeyService.regenerateApiKey(clientId);
        
        Map<String, String> result = new HashMap<>();
        result.put("clientId", client.getClientId());
        result.put("apiKey", client.getApiKey());
        result.put("message", "请妥善保管新的API Key，旧Key已失效！");
        
        return ApiResponse.success("API Key重新生成成功", result);
    }

    /**
     * 启用客户端
     */
    @PutMapping("/{clientId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> enableClient(@PathVariable String clientId) {
        log.info("启用客户端: clientId={}", clientId);
        
        apiKeyService.updateClientStatus(clientId, 1);
        
        return ApiResponse.success("客户端已启用");
    }

    /**
     * 禁用客户端
     */
    @PutMapping("/{clientId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> disableClient(@PathVariable String clientId) {
        log.info("禁用客户端: clientId={}", clientId);
        
        apiKeyService.updateClientStatus(clientId, 0);
        
        return ApiResponse.success("客户端已禁用");
    }

    /**
     * 获取客户端列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ApiClient>> listClients() {
        log.info("获取客户端列表");
        
        List<ApiClient> clients = apiKeyService.listClients();
        
        // 隐藏敏感信息
        clients.forEach(client -> {
            if (client.getApiKey() != null) {
                client.setApiKey(client.getApiKey().substring(0, 15) + "...");
            }
        });
        
        return ApiResponse.success(clients);
    }

    /**
     * 获取客户端详情
     */
    @GetMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ApiClient> getClient(@PathVariable String clientId) {
        log.info("获取客户端详情: clientId={}", clientId);
        
        ApiClient client = apiKeyService.getClient(clientId);
        
        if (client == null) {
            return ApiResponse.error("客户端不存在");
        }
        
        // 隐藏API Key
        if (client.getApiKey() != null) {
            client.setApiKey(client.getApiKey().substring(0, 15) + "...");
        }
        
        return ApiResponse.success(client);
    }

    /**
     * 删除客户端
     */
    @DeleteMapping("/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteClient(@PathVariable String clientId) {
        log.info("删除客户端: clientId={}", clientId);
        
        apiKeyService.deleteClient(clientId);
        
        return ApiResponse.success("客户端已删除");
    }

    /**
     * 创建客户端请求
     */
    @Data
    public static class CreateClientRequest {
        private String clientName;
        private String scopes;
        private String description;
    }
}
