package com.example.springboottest.config;

import com.example.springboottest.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * JWT认证过滤器
 * 处理每个请求中的JWT令牌验证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // 无需认证的路径
    private static final List<String> PERMIT_ALL_PATHS = Arrays.asList(
            "/auth/", "/health", "/weather/", "/test/public", "/error"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        try {
            // 检查是否为无需认证的路径
            if (isPermitAllPath(request.getRequestURI())) {
                filterChain.doFilter(request, response);
                return;
            }

            // 从请求头中获取JWT令牌
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                try {
                    // 检查令牌类型
                    String tokenType = jwtUtil.getTokenType(jwt);
                    
                    if ("client".equals(tokenType)) {
                        // 处理客户端令牌
                        handleClientToken(jwt, request);
                    } else {
                        // 处理用户令牌
                        handleUserToken(jwt, request);
                    }
                } catch (ExpiredJwtException e) {
                    log.warn("JWT令牌已过期: {}", e.getMessage());
                    request.setAttribute("expired", true);
                } catch (UnsupportedJwtException e) {
                    log.warn("不支持的JWT令牌: {}", e.getMessage());
                    request.setAttribute("unsupported", true);
                } catch (MalformedJwtException e) {
                    log.warn("JWT令牌格式错误: {}", e.getMessage());
                    request.setAttribute("malformed", true);
                } catch (SecurityException e) {
                    log.warn("JWT令牌签名验证失败: {}", e.getMessage());
                    request.setAttribute("signature", true);
                } catch (IllegalArgumentException e) {
                    log.warn("JWT令牌参数非法: {}", e.getMessage());
                    request.setAttribute("illegal", true);
                } catch (Exception e) {
                    log.error("JWT令牌处理异常: {}", e.getMessage(), e);
                    request.setAttribute("error", true);
                }
            } else {
                log.debug("请求中未包含JWT令牌: {}", request.getRequestURI());
            }

        } catch (Exception e) {
            log.error("JWT认证过滤器处理异常: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 处理用户令牌
     */
    private void handleUserToken(String jwt, HttpServletRequest request) {
        // 从令牌中提取用户名
        String username = jwtUtil.getUsernameFromToken(jwt);

        // 如果用户名不为空且当前没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 加载用户详情
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 验证令牌
            if (jwtUtil.validateToken(jwt, username)) {
                // 创建认证令牌
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置安全上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("用户 {} 认证成功", username);
            } else {
                log.warn("JWT令牌验证失败，用户: {}", username);
            }
        }
    }
    
    /**
     * 处理客户端令牌
     */
    private void handleClientToken(String jwt, HttpServletRequest request) {
        // 从令牌中提取客户端ID
        String clientId = jwtUtil.getClientIdFromToken(jwt);
        
        // 如果客户端ID不为空且当前没有认证信息
        if (clientId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 验证客户端令牌
            if (jwtUtil.validateClientToken(jwt, clientId)) {
                // 获取权限范围
                String scopes = jwtUtil.getScopesFromToken(jwt);
                
                // 创建客户端用户详情（使用客户端ID作为principal）
                UserDetails clientDetails = User.builder()
                        .username(clientId)
                        .password("") // 客户端认证不需要密码
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_CLIENT")))
                        .build();
                
                // 创建认证令牌
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                clientDetails, null, clientDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 设置安全上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("客户端 {} 认证成功，权限范围: {}", clientId, scopes);
            } else {
                log.warn("客户端JWT令牌验证失败，客户端ID: {}", clientId);
            }
        }
    }

    /**
     * 从请求中提取JWT令牌
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 检查是否为无需认证的路径
     */
    private boolean isPermitAllPath(String requestURI) {
        return PERMIT_ALL_PATHS.stream().anyMatch(requestURI::startsWith);
    }
}