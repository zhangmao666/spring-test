package com.example.springboottest.modules.log.aspect;

import com.alibaba.fastjson2.JSON;
import com.example.springboottest.modules.log.annotation.Log;
import com.example.springboottest.modules.log.entity.OperationLog;
import com.example.springboottest.modules.log.service.OperationLogService;
import com.example.springboottest.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

/**
 * 操作日志记录切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogService operationLogService;

    /**
     * 存储开始时间的ThreadLocal
     */
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    @Before("@annotation(controllerLog)")
    public void doBefore(JoinPoint joinPoint, Log controllerLog) {
        START_TIME.set(System.currentTimeMillis());
    }

    /**
     * 处理正常返回
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 处理异常
     */
    @AfterThrowing(pointcut = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    /**
     * 记录操作日志
     */
    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            // 获取当前请求
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();

            // 获取当前用户
            String username = getCurrentUsername();

            // 构建操作日志
            OperationLog operationLog = OperationLog.builder()
                    .title(controllerLog.title())
                    .businessType(controllerLog.businessType().getCode())
                    .method(joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + "()")
                    .requestMethod(request.getMethod())
                    .operatorName(username)
                    .requestUrl(request.getRequestURI())
                    .ipAddress(IpUtil.getClientIp(request))
                    .operationTime(LocalDateTime.now())
                    .build();

            // 设置请求参数
            if (controllerLog.isSaveRequestData()) {
                String params = getRequestParams(joinPoint);
                operationLog.setRequestParam(truncateString(params, 2000));
            }

            // 设置响应结果
            if (controllerLog.isSaveResponseData() && jsonResult != null) {
                operationLog.setJsonResult(truncateString(JSON.toJSONString(jsonResult), 2000));
            }

            // 设置状态和错误信息
            if (e != null) {
                operationLog.setStatus(0);
                operationLog.setErrorMsg(truncateString(e.getMessage(), 2000));
            } else {
                operationLog.setStatus(1);
            }

            // 计算耗时
            Long startTime = START_TIME.get();
            if (startTime != null) {
                operationLog.setCostTime(System.currentTimeMillis() - startTime);
            }

            // 异步保存日志
            operationLogService.recordOperationLog(operationLog);
        } catch (Exception ex) {
            log.error("记录操作日志异常: {}", ex.getMessage());
        } finally {
            START_TIME.remove();
        }
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.debug("获取当前用户失败: {}", e.getMessage());
        }
        return "anonymous";
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (args == null || args.length == 0) {
            return "";
        }

        StringBuilder params = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg != null && !isFilterObject(arg)) {
                if (params.length() > 0) {
                    params.append(", ");
                }
                String paramName = (parameterNames != null && i < parameterNames.length) ? parameterNames[i] : "arg" + i;
                params.append(paramName).append("=");
                try {
                    params.append(JSON.toJSONString(arg));
                } catch (Exception e) {
                    params.append(arg.toString());
                }
            }
        }
        return params.toString();
    }

    /**
     * 判断是否需要过滤的对象
     */
    private boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection<?> collection = (Collection<?>) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map<?, ?> map = (Map<?, ?>) o;
            for (Object value : map.entrySet()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest 
                || o instanceof HttpServletResponse;
    }

    /**
     * 截断字符串
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}
