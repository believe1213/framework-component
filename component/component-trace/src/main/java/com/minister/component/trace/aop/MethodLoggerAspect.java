package com.minister.component.trace.aop;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.minister.component.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * MethodLogger 注解实现
 *
 * @author QIUCHANGQING620
 * @date 2021-10-25 09:21
 */
@Aspect
@Slf4j
@Component
public class MethodLoggerAspect {

    @Pointcut("@annotation(com.minister.component.trace.aop.MethodLogger)")
    public void pointcut() {
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
        // 获取 MethodLogger
        MethodLogger methodLogger = AnnotationUtils.findAnnotation(method, MethodLogger.class);

        String methodDescription = methodLogger.description();
        boolean isPrintResult = methodLogger.isPrintResult();

        // 打印入参
        String description = StringUtils.isBlank(methodDescription) ? method.getName() : methodDescription;
        // ", Args : \n%s"
        String argsLog = getArgsLog(methodLogger, methodSignature.getParameterNames(), proceedingJoinPoint.getArgs());
        String preLog = String.format("Method Execute : %s%s", description, argsLog);
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
            String afterLog = String.format("Method Execute : %s, Method Consuming(MS) : %s.%s", description, costMills, resLog);
            log.info(afterLog);
        }

        return result;
    }

    private String getArgsLog(MethodLogger methodLogger, String[] parameterNames, Object[] args) {
        boolean isPrintParam = methodLogger.isPrintParam();
        if (!isPrintParam) {
            return StringUtils.EMPTY;
        }

        Set<String> excludeParamSet = CollUtil.newHashSet(methodLogger.excludeParam());
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
