package com.example.springboottest.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 课程状态枚举
 */
@Getter
public enum CourseStatus {
    
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布");
    
    @EnumValue
    private final Integer code;
    
    @JsonValue
    private final String desc;
    
    CourseStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static CourseStatus fromCode(Integer code) {
        for (CourseStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
