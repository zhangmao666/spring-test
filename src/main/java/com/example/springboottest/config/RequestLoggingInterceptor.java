package com.example.springboottest.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.Duration;
import java.time.Instant;

/**
 * 请求日志拦截器
 * 记录每个请求的详细信息和响应时间
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    
    private static final String START_TIME = "startTime";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, Instant.now());
        
        // 记录请求信息
        log.info("请求开始 - 方法: {}, URI: {}, 客户端IP: {}", 
            request.getMethod(), 
            request.getRequestURI(), 
            getClientIp(request));
            
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) {
        // 可以在这里添加额外的处理逻辑
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        Instant startTime = (Instant) request.getAttribute(START_TIME);
        if (startTime != null) {
            Duration duration = Duration.between(startTime, Instant.now());
            
            if (ex != null) {
                log.error("请求异常 - 方法: {}, URI: {}, 状态码: {}, 耗时: {}ms, 异常: {}", 
                    request.getMethod(), 
                    request.getRequestURI(), 
                    response.getStatus(),
                    duration.toMillis(),
                    ex.getMessage());
            } else {
                log.info("请求结束 - 方法: {}, URI: {}, 状态码: {}, 耗时: {}ms", 
                    request.getMethod(), 
                    request.getRequestURI(), 
                    response.getStatus(),
                    duration.toMillis());
            }
            
            // 慢请求告警（超过3秒）
            if (duration.toMillis() > 3000) {
                log.warn("慢请求告警 - URI: {}, 耗时: {}ms", 
                    request.getRequestURI(), 
                    duration.toMillis());
            }
        }
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
