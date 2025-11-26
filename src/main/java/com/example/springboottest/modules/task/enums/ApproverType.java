package com.example.springboottest.modules.task.enums;

import lombok.Getter;

/**
 * 审批人类型枚举
 */
@Getter
public enum ApproverType {
    ROLE("角色", "按角色指定审批人"),
    USER("用户", "指定具体用户审批");

    private final String displayName;
    private final String description;

    ApproverType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public static ApproverType fromName(String name) {
        for (ApproverType type : ApproverType.values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的审批人类型: " + name);
    }
}
