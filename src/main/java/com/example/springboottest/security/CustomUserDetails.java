package com.example.springboottest.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 自定义 UserDetails 实现，包含更多用户信息
 */
@Getter
public class CustomUserDetails extends User {

    /**
     * 用户ID
     */
    private final Long userId;

    /**
     * 邮箱
     */
    private final String email;

    public CustomUserDetails(Long userId, String username, String password, String email,
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
        this.email = email;
    }

    public CustomUserDetails(Long userId, String username, String password, String email,
                             boolean enabled, boolean accountNonExpired,
                             boolean credentialsNonExpired, boolean accountNonLocked,
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
        this.email = email;
    }
}
