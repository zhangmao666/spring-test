package com.example.springboottest.annotation;

import java.lang.annotation.*;

/**
 * 用于注入当前登录用户ID的注解
 * 使用方式：在Controller方法参数中使用 @CurrentUser Long userId
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
