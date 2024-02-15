package com.minister.component.utils;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static cn.hutool.core.text.CharPool.DOT;

/**
 * AopUtil
 *
 * @author QIUCHANGQING620
 * @date 2020-07-27 20:22
 */
@Slf4j
public class AopUtil {

    private AopUtil() {
    }

    /**
     * 获取注解内容
     *
     * @param joinPoint      切点
     * @param annotationType 注解类型
     * @param <A>            注解
     * @return 注解内容
     * @throws NoSuchMethodException 异常
     */
    public static <A extends Annotation> A findAnnotation(JoinPoint joinPoint, @Nullable Class<A> annotationType) throws NoSuchMethodException {
        // 获取当前方法签名
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new NoSuchMethodException("This annotation is only valid on a method");
        }
        MethodSignature methodSignature = (MethodSignature) signature;
        // 获取当前类
        Class<?> targetClass = joinPoint.getTarget().getClass();
        // 获取当前方法
        Method method = targetClass.getMethod(signature.getName(), methodSignature.getParameterTypes());
        log.debug("findAnnotation on method : {}", targetClass.getName() + DOT + method.getName());

        return AnnotationUtils.findAnnotation(method, annotationType);
    }

}
