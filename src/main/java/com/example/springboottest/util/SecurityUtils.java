package com.example.springboottest.util;

import com.example.springboottest.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * 安全工具类 - 获取当前登录用户信息
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // 私有构造函数，防止实例化
    }

    /**
     * 获取当前认证信息
     *
     * @return 认证信息，未登录返回 null
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户的 UserDetails
     *
     * @return UserDetails，未登录返回 empty
     */
    public static Optional<UserDetails> getCurrentUserDetails() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserDetails) {
            return Optional.of((UserDetails) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    /**
     * 获取当前登录用户的自定义 UserDetails
     *
     * @return CustomUserDetails，未登录或非自定义类型返回 empty
     */
    public static Optional<CustomUserDetails> getCurrentCustomUserDetails() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails) {
            return Optional.of((CustomUserDetails) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，未登录返回 null
     */
    public static Long getCurrentUserId() {
        return getCurrentCustomUserDetails()
                .map(CustomUserDetails::getUserId)
                .orElse(null);
    }

    /**
     * 获取当前登录用户ID（必须存在）
     *
     * @return 用户ID
     * @throws IllegalStateException 如果用户未登录
     */
    public static Long requireCurrentUserId() {
        return getCurrentCustomUserDetails()
                .map(CustomUserDetails::getUserId)
                .orElseThrow(() -> new IllegalStateException("用户未登录或无法获取用户ID"));
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名，未登录返回 null
     */
    public static String getCurrentUsername() {
        return getCurrentUserDetails()
                .map(UserDetails::getUsername)
                .orElse(null);
    }

    /**
     * 获取当前登录用户名（必须存在）
     *
     * @return 用户名
     * @throws IllegalStateException 如果用户未登录
     */
    public static String requireCurrentUsername() {
        return getCurrentUserDetails()
                .map(UserDetails::getUsername)
                .orElseThrow(() -> new IllegalStateException("用户未登录"));
    }

    /**
     * 获取当前登录用户邮箱
     *
     * @return 邮箱，未登录返回 null
     */
    public static String getCurrentUserEmail() {
        return getCurrentCustomUserDetails()
                .map(CustomUserDetails::getEmail)
                .orElse(null);
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return true 已登录，false 未登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null 
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 检查当前用户是否具有指定角色
     *
     * @param role 角色名（不需要 ROLE_ 前缀）
     * @return true 拥有该角色，false 没有该角色
     */
    public static boolean hasRole(String role) {
        return getCurrentUserDetails()
                .map(user -> user.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role)))
                .orElse(false);
    }

    /**
     * 检查当前用户是否具有指定权限
     *
     * @param authority 权限名
     * @return true 拥有该权限，false 没有该权限
     */
    public static boolean hasAuthority(String authority) {
        return getCurrentUserDetails()
                .map(user -> user.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals(authority)))
                .orElse(false);
    }
}
