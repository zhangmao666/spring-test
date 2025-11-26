package com.example.springboottest.exception;

/**
 * 资源重复异常
 */
public class DuplicateResourceException extends BusinessException {
    
    public DuplicateResourceException(String message) {
        super(409, message);
    }
    
    public DuplicateResourceException(String resourceName, String identifier) {
        super(409, String.format("%s 已存在: %s", resourceName, identifier));
    }
}
