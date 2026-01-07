package com.example.springboottest.modules.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.modules.log.entity.LoginLog;
import com.example.springboottest.modules.log.repository.LoginLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 登录日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;

    /**
     * 异步记录登录日志
     */
    @Async
    public void recordLoginLog(String username, String ipAddress, String browser, 
                               String os, Integer status, String msg) {
        try {
            LoginLog loginLog = LoginLog.builder()
                    .username(username)
                    .ipAddress(ipAddress)
                    .browser(browser)
                    .os(os)
                    .status(status)
                    .msg(msg)
                    .loginTime(LocalDateTime.now())
                    .build();
            loginLogRepository.insert(loginLog);
        } catch (Exception e) {
            log.error("记录登录日志失败: {}", e.getMessage());
        }
    }

    /**
     * 分页查询登录日志
     */
    public IPage<LoginLog> getLoginLogList(Integer page, Integer size, String username, 
                                           Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(username)) {
            wrapper.like(LoginLog::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(LoginLog::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(LoginLog::getLoginTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(LoginLog::getLoginTime, endTime);
        }
        
        wrapper.orderByDesc(LoginLog::getLoginTime);
        
        return loginLogRepository.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 删除登录日志
     */
    public void deleteLoginLog(Long id) {
        loginLogRepository.deleteById(id);
    }

    /**
     * 批量删除登录日志
     */
    public void batchDeleteLoginLog(Long[] ids) {
        for (Long id : ids) {
            loginLogRepository.deleteById(id);
        }
    }

    /**
     * 清空登录日志
     */
    public void clearLoginLog() {
        loginLogRepository.delete(new LambdaQueryWrapper<>());
    }
}
