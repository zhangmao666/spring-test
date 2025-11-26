package com.example.springboottest.enums;

import lombok.Getter;

/**
 * 任务状态枚举
 */
@Getter
public enum TaskStatus {
    DRAFT("草稿", "任务已创建但未提交"),
    PENDING("待审批", "任务已提交，等待审批"),
    IN_PROGRESS("审批中", "任务正在审批流程中"),
    APPROVED("已通过", "任务审批通过"),
    REJECTED("已驳回", "任务被驳回"),
    WITHDRAWN("已撤回", "任务被创建人撤回"),
    CANCELLED("已取消", "任务被取消"),
    COMPLETED("已完成", "任务执行完成");

    private final String displayName;
    private final String description;

    TaskStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 根据枚举名称获取枚举
     */
    public static TaskStatus fromName(String name) {
        for (TaskStatus status : TaskStatus.values()) {
            if (status.name().equals(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的任务状态: " + name);
    }

    /**
     * 判断是否可以提交审批
     */
    public boolean canSubmit() {
        return this == DRAFT || this == REJECTED;
    }

    /**
     * 判断是否可以撤回
     */
    public boolean canWithdraw() {
        return this == PENDING || this == IN_PROGRESS;
    }

    /**
     * 判断是否可以审批
     */
    public boolean canApprove() {
        return this == PENDING || this == IN_PROGRESS;
    }

    /**
     * 判断是否为终态
     */
    public boolean isFinalState() {
        return this == APPROVED || this == WITHDRAWN || this == CANCELLED || this == COMPLETED;
    }
}
