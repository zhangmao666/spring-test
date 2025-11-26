package com.example.springboottest.config;

// 注意：要启用此拦截器，需要在pom.xml中添加bucket4j依赖
// import com.example.springboottest.DTO.ApiResponse;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import io.github.bucket4j.Bandwidth;
// import io.github.bucket4j.Bucket;
// import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;

/**
 * API限流拦截器 - 示例实现
 * 
 * 使用方法：
 * 1. 在pom.xml中添加依赖:
 * <dependency>
 *     <groupId>com.github.vladimir-bukhtoyarov</groupId>
 *     <artifactId>bucket4j-core</artifactId>
 *     <version>8.1.1</version>
 * </dependency>
 * 
 * 2. 取消注释bucket4j相关导入
 * 3. 取消注释类实现代码
 * 4. 在WebConfig中注册此拦截器
 * 
 * 注意：此文件当前被注释掉以避免编译错误
 */
@Slf4j
// @Component  // 取消注释以启用
public class RateLimitInterceptor implements HandlerInterceptor {
    
    // 取消下面的注释以启用限流功能（需要先添加bucket4j依赖）
    
    /*
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 每个IP每分钟最多100次请求
    private static final int CAPACITY = 100;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String ip = getClientIp(request);
        Bucket bucket = cache.computeIfAbsent(ip, k -> createNewBucket());
        
        if (bucket.tryConsume(1)) {
            return true;
        } else {
            log.warn("请求频率超限 - IP: {}, URI: {}", ip, request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            
            ApiResponse<Object> apiResponse = ApiResponse.error(429, "请求过于频繁，请稍后再试");
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            return false;
        }
    }
    
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(CAPACITY, Refill.intervally(CAPACITY, REFILL_DURATION));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    */
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 占位符实现 - 添加bucket4j依赖后取消上面的注释并删除此方法
        log.debug("限流拦截器未启用，请添加bucket4j依赖");
        return true;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
