package com.example.springboottest.modules.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.modules.log.entity.OperationLog;
import com.example.springboottest.modules.log.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 操作日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    /**
     * 异步记录操作日志
     */
    @Async
    public void recordOperationLog(OperationLog operationLog) {
        try {
            operationLogRepository.insert(operationLog);
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage());
        }
    }

    /**
     * 分页查询操作日志
     */
    public IPage<OperationLog> getOperationLogList(Integer page, Integer size, String title,
                                                    String operatorName, Integer businessType,
                                                    Integer status, LocalDateTime startTime, 
                                                    LocalDateTime endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(title)) {
            wrapper.like(OperationLog::getTitle, title);
        }
        if (StringUtils.hasText(operatorName)) {
            wrapper.like(OperationLog::getOperatorName, operatorName);
        }
        if (businessType != null) {
            wrapper.eq(OperationLog::getBusinessType, businessType);
        }
        if (status != null) {
            wrapper.eq(OperationLog::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(OperationLog::getOperationTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperationLog::getOperationTime, endTime);
        }
        
        wrapper.orderByDesc(OperationLog::getOperationTime);
        
        return operationLogRepository.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 获取操作日志详情
     */
    public OperationLog getOperationLogById(Long id) {
        return operationLogRepository.selectById(id);
    }

    /**
     * 删除操作日志
     */
    public void deleteOperationLog(Long id) {
        operationLogRepository.deleteById(id);
    }

    /**
     * 批量删除操作日志
     */
    public void batchDeleteOperationLog(Long[] ids) {
        for (Long id : ids) {
            operationLogRepository.deleteById(id);
        }
    }

    /**
     * 清空操作日志
     */
    public void clearOperationLog() {
        operationLogRepository.delete(new LambdaQueryWrapper<>());
    }
}
