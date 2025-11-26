package com.example.springboottest.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.ApiClient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * API客户端Repository
 */
@Mapper
public interface ApiClientRepository extends BaseMapper<ApiClient> {
    
    /**
     * 根据客户端ID查找
     * 
     * @param clientId 客户端ID
     * @return API客户端
     */
    @Select("SELECT * FROM api_clients WHERE client_id = #{clientId}")
    ApiClient selectByClientId(String clientId);
    
    /**
     * 根据客户端ID和状态查找
     * 
     * @param clientId 客户端ID
     * @param status 状态
     * @return API客户端
     */
    @Select("SELECT * FROM api_clients WHERE client_id = #{clientId} AND status = #{status}")
    ApiClient selectByClientIdAndStatus(String clientId, Integer status);
    
    /**
     * 检查客户端ID是否存在
     * 
     * @param clientId 客户端ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) FROM api_clients WHERE client_id = #{clientId}")
    int existsByClientId(String clientId);
    
    /**
     * 根据API Key查找
     * 
     * @param apiKey API密钥
     * @return API客户端
     */
    @Select("SELECT * FROM api_clients WHERE api_key = #{apiKey}")
    ApiClient selectByApiKey(String apiKey);
    
    /**
     * 根据客户端ID查找（返回Optional）
     */
    default Optional<ApiClient> findByClientId(String clientId) {
        return Optional.ofNullable(selectByClientId(clientId));
    }
    
    /**
     * 根据客户端ID和状态查找（返回Optional）
     */
    default Optional<ApiClient> findByClientIdAndStatus(String clientId, Integer status) {
        return Optional.ofNullable(selectByClientIdAndStatus(clientId, status));
    }
    
    /**
     * 根据API Key查找（返回Optional）
     */
    default Optional<ApiClient> findByApiKey(String apiKey) {
        return Optional.ofNullable(selectByApiKey(apiKey));
    }
}
