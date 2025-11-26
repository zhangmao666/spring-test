package com.example.springboottest.enums;

import lombok.Getter;

/**
 * 审批结果枚举
 */
@Getter
public enum ApprovalResult {
    PENDING("待审批", "等待审批人处理"),
    APPROVED("已通过", "审批已通过"),
    REJECTED("已驳回", "审批已驳回"),
    TRANSFERRED("已转交", "已转交给他人"),
    WITHDRAWN("已撤回", "已被撤回");

    private final String displayName;
    private final String description;

    ApprovalResult(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 根据枚举名称获取枚举
     */
    public static ApprovalResult fromName(String name) {
        for (ApprovalResult result : ApprovalResult.values()) {
            if (result.name().equals(name)) {
                return result;
            }
        }
        throw new IllegalArgumentException("未知的审批结果: " + name);
    }
}
