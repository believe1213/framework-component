package com.minister.component.redis.distributedlock.aop;

import com.minister.component.redis.distributedlock.RedisLockUtil;
import com.minister.component.redis.distributedlock.impl.RedisSimpleLockUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 分布式锁注解
 *
 * @author QIUCHANGQING620
 * @date 2020-07-27 19:46
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLock {

    /**
     * 锁名
     */
    @AliasFor("value")
    String key() default StringUtils.EMPTY;

    /**
     * 锁时间(s)
     */
    long lockTime() default 60;

    /**
     * 没有抢到锁，是否等待
     */
    boolean isWait() default false;

    /**
     * 等待锁时间(s)
     */
    long waitTime() default 60;

    /**
     * 锁类型
     */
    Class<? extends RedisLockUtil> lockClass() default RedisSimpleLockUtil.class;

    @AliasFor("key")
    String value() default StringUtils.EMPTY;

}
