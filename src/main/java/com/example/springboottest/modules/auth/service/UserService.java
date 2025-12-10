package com.example.springboottest.modules.auth.service;

import com.example.springboottest.modules.auth.dto.LoginRequest;
import com.example.springboottest.modules.auth.dto.LoginResponse;
import com.example.springboottest.modules.auth.entity.User;
import com.example.springboottest.modules.auth.repository.UserRepository;
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
     */
    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        
        Optional<User> userOptional = userRepository.findByUsernameAndStatus(username, 1);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        User user = userOptional.get();
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getId());

        String appendAccessToken = "Bearer " + accessToken;
        
        log.info("用户 {} 登录成功", username);
        
        return new LoginResponse(appendAccessToken, user.getId(), user.getUsername(), user.getEmail());
    }
    
    /**
     * 注册用户
     */
    public User register(String username, String password, String email) {
        if (userRepository.existsByUsername(username) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        if (userRepository.existsByEmail(email) > 0) {
            throw new RuntimeException("邮箱已存在");
        }
        
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, encodedPassword, email);
        userRepository.insert(user);
        
        log.info("用户 {} 注册成功", username);
        
        return user;
    }
    
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userRepository.selectById(id));
    }
}
