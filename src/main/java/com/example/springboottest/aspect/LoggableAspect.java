package com.example.springboottest.aspect;

import com.example.springboottest.annotation.Loggale;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @Author: zm
 * @Created: 2025/9/30 下午3:19
 * @Description:
 */
@Aspect
@Component
public class LoggableAspect {

    @Around("@annotation(loggale)")
    public Object logExecution(ProceedingJoinPoint joinPoint, Loggale loggale) throws Throwable{
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【日志】开始："+loggale.value()+",方法："+methodName);

        long start =System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;
        System.out.println("【日志】结束："+loggale.value()+",方法："+methodName+",耗时："+duration+"ms");
        return result;
    }
}