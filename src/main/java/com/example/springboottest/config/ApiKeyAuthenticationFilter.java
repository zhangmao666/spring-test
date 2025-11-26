package com.example.springboottest.config;

import com.example.springboottest.entity.ApiClient;
import com.example.springboottest.repository.ApiClientRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * API Key认证过滤器
 * 用于验证第三方客户端的API Key
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiClientRepository apiClientRepository;
    
    // API Key请求头名称
    private static final String API_KEY_HEADER = "X-API-Key";
    
    // 无需API Key认证的路径
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/auth/login",
        "/auth/register",
        "/mock/",
        "/health",
        "/api-docs",
        "/swagger-ui"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        String requestUri = request.getRequestURI();
        
        // 跳过无需认证的路径
        if (shouldSkipFilter(requestUri)) {
            log.debug("跳过API Key验证: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 提取API Key
            String apiKey = extractApiKey(request);
            
            if (StringUtils.hasText(apiKey)) {
                log.debug("检测到API Key: {}...", apiKey.substring(0, Math.min(10, apiKey.length())));
                
                // 验证API Key
                Optional<ApiClient> clientOptional = apiClientRepository.findByApiKey(apiKey);
                
                if (clientOptional.isPresent()) {
                    ApiClient client = clientOptional.get();
                    
                    // 检查客户端状态
                    if (client.getStatus() != 1) {
                        log.warn("API Key对应的客户端已禁用: {}", client.getClientId());
                        request.setAttribute("apikey_disabled", true);
                    } else {
                        // 设置认证信息
                        setAuthentication(client, request);
                        log.info("API Key认证成功: clientId={}, clientName={}", 
                                client.getClientId(), client.getClientName());
                    }
                } else {
                    log.warn("无效的API Key: {}...", apiKey.substring(0, Math.min(10, apiKey.length())));
                    request.setAttribute("apikey_invalid", true);
                }
            } else {
                log.debug("请求中未包含API Key: {}", requestUri);
            }
            
        } catch (Exception e) {
            log.error("API Key认证处理异常: {}", e.getMessage(), e);
            request.setAttribute("apikey_error", true);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取API Key
     */
    private String extractApiKey(HttpServletRequest request) {
        // 方式1: 从请求头获取
        String apiKey = request.getHeader(API_KEY_HEADER);
        
        // 方式2: 从查询参数获取（备用，不推荐）
        if (!StringUtils.hasText(apiKey)) {
            apiKey = request.getParameter("apiKey");
        }
        
        return apiKey;
    }

    /**
     * 设置Spring Security认证信息
     */
    private void setAuthentication(ApiClient client, HttpServletRequest request) {
        // 解析权限范围
        List<SimpleGrantedAuthority> authorities = parseScopes(client.getScopes());
        
        // 创建认证对象（使用clientId作为principal）
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                client.getClientId(),
                null,
                authorities
            );
        
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        // 设置到安全上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 解析权限范围
     */
    private List<SimpleGrantedAuthority> parseScopes(String scopes) {
        if (!StringUtils.hasText(scopes)) {
            return List.of(new SimpleGrantedAuthority("SCOPE_read"));
        }
        
        return Arrays.stream(scopes.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .collect(Collectors.toList());
    }

    /**
     * 判断是否应该跳过此过滤器
     */
    private boolean shouldSkipFilter(String requestUri) {
        return EXCLUDED_PATHS.stream()
                .anyMatch(requestUri::startsWith);
    }
}
