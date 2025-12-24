package com.example.springboottest.modules.auth.service;

import com.example.springboottest.exception.BusinessException;
import com.example.springboottest.modules.auth.dto.LoginRequest;
import com.example.springboottest.modules.auth.dto.LoginResponse;
import com.example.springboottest.modules.auth.entity.User;
import com.example.springboottest.modules.auth.repository.UserRepository;
import com.example.springboottest.modules.log.service.LoginLogService;
import com.example.springboottest.util.IpUtil;
import com.example.springboottest.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * 用户服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginLogService loginLogService;
    
    /**
     * 用户登录
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        
        // 获取请求信息
        String ipAddress = "";
        String browser = "";
        String os = "";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ipAddress = IpUtil.getClientIp(request);
            browser = IpUtil.getBrowser(request);
            os = IpUtil.getOs(request);
        }
        
        Optional<User> userOptional = userRepository.findByUsernameAndStatus(username, 1);
        if (userOptional.isEmpty()) {
            // 记录登录失败日志
            loginLogService.recordLoginLog(username, ipAddress, browser, os, 0, "用户不存在或已禁用");
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        User user = userOptional.get();
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // 记录登录失败日志
            loginLogService.recordLoginLog(username, ipAddress, browser, os, 0, "密码错误");
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getId());
        
        // 记录登录成功日志
        loginLogService.recordLoginLog(username, ipAddress, browser, os, 1, "登录成功");
        
        log.info("用户 {} 登录成功", username);
        
        // 返回纯token，前端会添加Bearer前缀
        return new LoginResponse(accessToken, user.getId(), user.getUsername(), user.getEmail());
    }
    
    /**
     * 注册用户
     */
    @Transactional
    public User register(String username, String password, String email) {
        if (userRepository.existsByUsername(username) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        if (userRepository.existsByEmail(email) > 0) {
            throw new BusinessException("邮箱已存在");
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
