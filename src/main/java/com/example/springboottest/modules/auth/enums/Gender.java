package com.example.springboottest.modules.auth.enums;

/**
 * 性别枚举
 */
public enum Gender {
    MALE("男"),
    FEMALE("女"),
    UNKNOWN("未知");

    private final String description;

    Gender(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
