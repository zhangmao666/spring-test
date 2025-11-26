package com.example.springboottest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: zm
 * @Created: 2025/9/30 下午3:15
 * @Description:
 */
@Target({ElementType.METHOD}) // 可以用于方法
@Retention(RetentionPolicy.RUNTIME)  // 运行时可见
public @interface Loggale {

    String value() default "执行操作";
}