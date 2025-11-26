package com.example.springboottest.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    /**
     * 生成JWT令牌
     * 
     * @param username 用户名
     * @param userId 用户ID
     * @return JWT令牌
     */
    public String generateToken(String username, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * 从令牌中获取用户名
     * 
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }
    
    /**
     * 从令牌中获取用户ID
     * 
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * 从令牌中获取过期时间
     * 
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }
    
    /**
     * 验证令牌是否有效
     * 
     * @param token JWT令牌
     * @param username 用户名
     * @return 是否有效
     */
    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            return tokenUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查令牌是否过期
     * 
     * @param token JWT令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    /**
     * 生成客户端JWT令牌
     * 
     * @param clientId 客户端ID
     * @param clientName 客户端名称
     * @param scopes 权限范围
     * @param customExpiration 自定义过期时间（毫秒）
     * @return JWT令牌
     */
    public String generateClientToken(String clientId, String clientName, 
                                      String scopes, Long customExpiration) {
        Date now = new Date();
        Long expirationTime = customExpiration != null ? customExpiration : expiration;
        Date expiryDate = new Date(now.getTime() + expirationTime);
        
        return Jwts.builder()
                .setSubject(clientId)
                .claim("clientName", clientName)
                .claim("scopes", scopes)
                .claim("type", "client")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * 验证客户端令牌是否有效
     * 
     * @param token JWT令牌
     * @param clientId 客户端ID
     * @return 是否有效
     */
    public boolean validateClientToken(String token, String clientId) {
        try {
            Claims claims = getClaimsFromToken(token);
            String tokenClientId = claims.getSubject();
            String tokenType = claims.get("type", String.class);
            
            return "client".equals(tokenType) 
                    && tokenClientId.equals(clientId) 
                    && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("客户端JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 从令牌中获取客户端ID
     * 
     * @param token JWT令牌
     * @return 客户端ID
     */
    public String getClientIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String tokenType = claims.get("type", String.class);
        if ("client".equals(tokenType)) {
            return claims.getSubject();
        }
        return null;
    }
    
    /**
     * 从令牌中获取权限范围
     * 
     * @param token JWT令牌
     * @return 权限范围
     */
    public String getScopesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("scopes", String.class);
    }
    
    /**
     * 检查令牌类型
     * 
     * @param token JWT令牌
     * @return 令牌类型（user/client）
     */
    public String getTokenType(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("type", String.class);
        } catch (Exception e) {
            return "user"; // 默认为用户令牌（兼容旧令牌）
        }
    }
    
    /**
     * 从令牌中获取Claims
     * 
     * @param token JWT令牌
     * @return Claims
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("JWT令牌已过期: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT令牌: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT令牌格式错误: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("JWT令牌签名验证失败: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT令牌参数非法: {}", e.getMessage());
            throw e;
        }
    }
}
