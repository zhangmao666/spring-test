package com.example.springboottest.config;

import com.example.springboottest.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 自定义认证入口点
 * 处理认证失败的情况，返回统一的401错误响应
 */
@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        log.warn("认证失败，请求路径: {}, 异常信息: {}", request.getRequestURI(), authException.getMessage());
        
        // 设置响应状态码为401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // 设置响应内容类型
        response.setContentType("application/json;charset=UTF-8");
        
        // 根据请求中的JWT异常属性确定具体错误信息
        String errorMessage = "认证失败，请先登录获取有效的访问令牌";
        
        if (request.getAttribute("expired") != null) {
            errorMessage = "访问令牌已过期，请重新登录";
        } else if (request.getAttribute("malformed") != null) {
            errorMessage = "访问令牌格式错误";
        } else if (request.getAttribute("signature") != null) {
            errorMessage = "访问令牌签名验证失败";
        } else if (request.getAttribute("unsupported") != null) {
            errorMessage = "不支持的访问令牌格式";
        } else if (request.getAttribute("illegal") != null) {
            errorMessage = "访问令牌参数非法";
        } else if (request.getAttribute("error") != null) {
            errorMessage = "访问令牌处理异常";
        }
        
        // 创建统一的错误响应
        ApiResponse<Object> errorResponse = ApiResponse.error(401, errorMessage);
        
        // 将响应对象转换为JSON并写入响应体
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}