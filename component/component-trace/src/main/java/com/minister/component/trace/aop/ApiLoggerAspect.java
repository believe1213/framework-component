package com.minister.component.trace.aop;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.minister.component.utils.AopUtil;
import com.minister.component.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * ApiLogger 注解实现
 *
 * @author QIUCHANGQING620
 * @date 2020-03-08 10:35
 */
@Order(0)
@Aspect
@Component
@Slf4j
public class ApiLoggerAspect {

    @Pointcut("@annotation(com.minister.component.trace.aop.ApiLogger)")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
    }

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取当前方法签名
        Signature signature = proceedingJoinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new NoSuchMethodException("This annotation is only valid on a method");
        }
        MethodSignature methodSignature = (MethodSignature) signature;
        // 获取当前类
        Class<?> targetClass = proceedingJoinPoint.getTarget().getClass();
        // 获取当前方法
        Method method = targetClass.getMethod(signature.getName(), methodSignature.getParameterTypes());
        // 获取 ApiLogger
        ApiLogger apiLogger = AopUtil.findAnnotation(proceedingJoinPoint, ApiLogger.class);

        String methodDescription = apiLogger.description();
        boolean isPrintResult = apiLogger.isPrintResult();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();
        String uri = request.getRequestURI();

        // 打印入参
        // ", Description : %s"
        String desLog = StringUtils.isBlank(methodDescription) ? StringUtils.EMPTY : String.format(", Description : %s", methodDescription);
        // ", Args : \n%s"
        String argsLog = getArgsLog(apiLogger, methodSignature.getParameterNames(), proceedingJoinPoint.getArgs());
        String preLog = String.format("API Request : %s%s%s", uri, desLog, argsLog);
        log.info(preLog);

        Object result = null;
        try {
            // 执行
            result = proceedingJoinPoint.proceed();
        } finally {
            // 打印出参
            String costMills = String.valueOf(System.currentTimeMillis() - startTime);
            // " Response : \n%s"
            String resLog = (Objects.isNull(result) || !isPrintResult) ? StringUtils.EMPTY : String.format(" Response : \n%s", JacksonUtil.bean2Json(result));
            String afterLog = String.format("API Request : %s%s, Time Consuming(MS) : %s.%s", uri, desLog, costMills, resLog);
            log.info(afterLog);
        }

        return result;
    }

    @After("pointcut()")
    public void doAfter(JoinPoint joinPoint) throws Throwable {
    }

    private String getArgsLog(ApiLogger apiLogger, String[] parameterNames, Object[] args) {
        boolean isPrintParam = apiLogger.isPrintParam();
        if (!isPrintParam) {
            return StringUtils.EMPTY;
        }

        Set<String> excludeParamSet = CollUtil.newHashSet(apiLogger.excludeParam());
        Map<String, Object> printMap = Maps.newHashMap();
        for (int i = 0; i < parameterNames.length; i++) {
            String name = parameterNames[i];
            if (excludeParamSet.contains(name)) {
                continue;
            }
            printMap.put(name, args[i]);
        }

        return String.format(", Args : \n%s", JacksonUtil.bean2Json(printMap));
    }

}
