package com.example.springboottest.modules.task.enums;

import lombok.Getter;

/**
 * 审批动作枚举
 */
@Getter
public enum ApprovalAction {
    APPROVE("通过", "审批通过"),
    REJECT("驳回", "驳回到指定节点"),
    TRANSFER("转交", "转交给他人审批"),
    WITHDRAW("撤回", "撤回任务");

    private final String displayName;
    private final String description;

    ApprovalAction(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public static ApprovalAction fromName(String name) {
        for (ApprovalAction action : ApprovalAction.values()) {
            if (action.name().equals(name)) {
                return action;
            }
        }
        throw new IllegalArgumentException("未知的审批动作: " + name);
    }
}
