package com.minister.component.trace.aop;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 接口日志打印注解
 *
 * @author QIUCHANGQING620
 * @date 2020-03-08 10:32
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ApiLogger {

    /**
     * 接口描述
     */
    @AliasFor("value")
    String description() default StringUtils.EMPTY;

    /**
     * 是否打印入参
     */
    boolean isPrintParam() default true;

    /**
     * 无需打印的入参参数名称
     * isPrintParam 为 true 时生效
     */
    String[] excludeParam() default {};

    /**
     * 是否打印出参
     */
    boolean isPrintResult() default true;

    @AliasFor("description")
    String value() default StringUtils.EMPTY;

}
