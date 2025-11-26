package com.example.springboottest.config;

import com.example.springboottest.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 自定义访问拒绝处理器
 * 处理权限不足的情况，返回统一的403错误响应
 */
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, 
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        log.warn("访问被拒绝，请求路径: {}, 异常信息: {}", request.getRequestURI(), accessDeniedException.getMessage());
        
        // 设置响应状态码为403
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        // 设置响应内容类型
        response.setContentType("application/json;charset=UTF-8");
        
        // 创建统一的错误响应
        ApiResponse<Object> errorResponse = ApiResponse.error(403, "权限不足，无法访问该资源");
        
        // 将响应对象转换为JSON并写入响应体
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}