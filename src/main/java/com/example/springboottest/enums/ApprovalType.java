package com.example.springboottest.enums;

import lombok.Getter;

/**
 * 审批类型枚举
 */
@Getter
public enum ApprovalType {
    COUNTERSIGN("会签", "所有审批人都必须通过"),
    OR_SIGN("或签", "任意一个审批人通过即可");

    private final String displayName;
    private final String description;

    ApprovalType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 根据枚举名称获取枚举
     */
    public static ApprovalType fromName(String name) {
        for (ApprovalType type : ApprovalType.values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的审批类型: " + name);
    }
}
