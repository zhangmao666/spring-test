package com.example.springboottest.exception;

/**
 * 资源不存在异常
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String message) {
        super(404, message);
    }
    
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(404, String.format("%s 未找到: %s", resourceName, identifier));
    }
}
