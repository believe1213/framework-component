package com.minister.component.trace.aop;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 方法调用日志打印注解
 * <pre>
 *     此注解仅在通过依赖注入调用方法时生效
 *     生效场景：
 *     1. @Resource 或 @Autowired 注入后调用
 *     2. 通过 ApplicationContext 获取注入对象后调用
 *     3. 通过 AopContext.currentProxy() 调用
 *       a. 在启动类上添加@EnableAspectJAutoProxy(exposeProxy = true)
 *       b. ((Class) AopContext.currentProxy()).xx()
 * </pre>
 *
 * @author QIUCHANGQING620
 * @date 2021-10-25 09:18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface MethodLogger {

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
