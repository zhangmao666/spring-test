package com.example.springboottest.service;

import com.example.springboottest.entity.ApiClient;
import com.example.springboottest.repository.ApiClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * API Key管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiClientRepository apiClientRepository;

    /**
     * 生成API Key
     * 格式: apk_<32位UUID>
     */
    public String generateApiKey() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "apk_" + uuid;
    }

    /**
     * 创建新的API客户端
     */
    public ApiClient createClient(String clientName, String scopes, String description) {
        // 生成客户端ID
        String clientId = "client_" + System.currentTimeMillis();
        
        // 生成API Key
        String apiKey = generateApiKey();
        
        // 创建客户端
        ApiClient client = new ApiClient();
        client.setClientId(clientId);
        client.setClientName(clientName);
        client.setApiKey(apiKey);
        client.setScopes(scopes);
        client.setTokenExpiration(7200000L); // 2小时
        client.setStatus(1);
        client.setDescription(description);
        
        // 保存到数据库
        apiClientRepository.insert(client);
        
        log.info("创建API客户端成功: clientId={}, clientName={}", clientId, clientName);
        
        return client;
    }

    /**
     * 重新生成API Key
     */
    public ApiClient regenerateApiKey(String clientId) {
        ApiClient client = apiClientRepository.selectByClientId(clientId);
        if (client == null) {
            throw new RuntimeException("客户端不存在: " + clientId);
        }
        
        // 生成新的API Key
        String newApiKey = generateApiKey();
        client.setApiKey(newApiKey);
        
        // 更新数据库
        apiClientRepository.updateById(client);
        
        log.info("重新生成API Key: clientId={}, newApiKey={}...", clientId, newApiKey.substring(0, 15));
        
        return client;
    }

    /**
     * 启用/禁用客户端
     */
    public void updateClientStatus(String clientId, Integer status) {
        ApiClient client = apiClientRepository.selectByClientId(clientId);
        if (client == null) {
            throw new RuntimeException("客户端不存在: " + clientId);
        }
        
        client.setStatus(status);
        apiClientRepository.updateById(client);
        
        log.info("更新客户端状态: clientId={}, status={}", clientId, status);
    }

    /**
     * 获取客户端列表
     */
    public List<ApiClient> listClients() {
        return apiClientRepository.selectList(null);
    }

    /**
     * 根据ID获取客户端
     */
    public ApiClient getClient(String clientId) {
        return apiClientRepository.selectByClientId(clientId);
    }

    /**
     * 删除客户端
     */
    public void deleteClient(String clientId) {
        ApiClient client = apiClientRepository.selectByClientId(clientId);
        if (client == null) {
            throw new RuntimeException("客户端不存在: " + clientId);
        }
        
        apiClientRepository.deleteById(client.getId());
        
        log.info("删除客户端: clientId={}", clientId);
    }

    /**
     * 验证API Key
     */
    public boolean validateApiKey(String apiKey) {
        return apiClientRepository.findByApiKey(apiKey)
                .map(client -> client.getStatus() == 1)
                .orElse(false);
    }
}
