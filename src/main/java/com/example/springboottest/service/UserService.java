package com.example.springboottest.service;

import com.example.springboottest.DTO.LoginRequest;
import com.example.springboottest.DTO.LoginResponse;
import com.example.springboottest.entity.User;
import com.example.springboottest.repository.UserRepository;
import com.example.springboottest.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        
        // 查找用户
        Optional<User> userOptional = userRepository.findByUsernameAndStatus(username, 1);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        User user = userOptional.get();
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 生成JWT令牌
        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getId());
        
        log.info("用户 {} 登录成功", username);
        
        return new LoginResponse(accessToken, user.getId(), user.getUsername(), user.getEmail());
    }
    
    /**
     * 注册用户
     * 
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @return 用户信息
     */
    public User register(String username, String password, String email) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(email) > 0) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 加密密码
        String encodedPassword = passwordEncoder.encode(password);
        
        // 创建用户
        User user = new User(username, encodedPassword, email);
        userRepository.insert(user);
        
        log.info("用户 {} 注册成功", username);
        
        return user;
    }
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 根据ID查找用户
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userRepository.selectById(id));
    }
}
