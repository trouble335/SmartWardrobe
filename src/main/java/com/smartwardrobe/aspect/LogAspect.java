package com.smartwardrobe.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Slf4j
@Aspect
@Component
public class LogAspect {

    private static final String TRACE_ID = "traceId";

    @Around("execution(* com.smartwardrobe.controller..*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID, traceId);

        long startTime = System.currentTimeMillis();

        HttpServletRequest request = getRequest();
        String requestURI = request != null ? request.getRequestURI() : "unknown";
        String method = request != null ? request.getMethod() : "unknown";

        log.info("[{}] 请求开始: {} {}", traceId, method, requestURI);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.info("[{}] 请求成功: {} {} - 耗时{}ms", traceId, method, requestURI, duration);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            log.error("[{}] 请求失败: {} {} - 耗时{}ms - 错误: {}", traceId, method, requestURI, duration, e.getMessage());

            throw e;
        } finally {
            MDC.remove(TRACE_ID);
        }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
