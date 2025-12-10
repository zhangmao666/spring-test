package com.example.springboottest.service;

import com.example.springboottest.modules.auth.entity.User;
import com.example.springboottest.modules.auth.repository.UserRepository;
import com.example.springboottest.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

/**
 * 自定义用户详情服务
 * 实现Spring Security的UserDetailsService接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("正在加载用户信息: {}", username);
        
        Optional<User> userOptional = userRepository.findByUsernameAndStatus(username, 1);
        
        if (userOptional.isEmpty()) {
            log.warn("用户不存在或已禁用: {}", username);
            throw new UsernameNotFoundException("用户不存在或已禁用: " + username);
        }
        
        User user = userOptional.get();
        
        // 创建自定义 UserDetails 对象，包含用户ID和邮箱
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getStatus() == 1,  // enabled
                true,                    // accountNonExpired
                true,                    // credentialsNonExpired
                user.getStatus() == 1,  // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}