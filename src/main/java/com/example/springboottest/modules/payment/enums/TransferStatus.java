package com.example.springboottest.modules.payment.enums;

import lombok.Getter;

/**
 * 转账状态枚举
 */
@Getter
public enum TransferStatus {
    
    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败"),
    CLOSED(4, "已关闭");
    
    private final int code;
    private final String description;
    
    TransferStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public static TransferStatus fromCode(int code) {
        for (TransferStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知转账状态: " + code);
    }
}
