package com.example.springboottest.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 课程系统用户角色枚举
 */
@Getter
public enum CourseUserRole {
    
    ADMIN("ADMIN", "管理员"),
    TEACHER("TEACHER", "教师"),
    STUDENT("STUDENT", "学生");
    
    @EnumValue
    private final String code;
    
    @JsonValue
    private final String desc;
    
    CourseUserRole(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static CourseUserRole fromCode(String code) {
        for (CourseUserRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}
