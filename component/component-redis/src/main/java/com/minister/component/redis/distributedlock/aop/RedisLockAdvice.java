package com.minister.component.redis.distributedlock.aop;

import com.minister.component.redis.distributedlock.impl.RedisLocalLockUtil;
import com.minister.component.redis.distributedlock.impl.RedisSimpleLockUtil;
import com.minister.component.utils.AopUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解实现
 *
 * @author QIUCHANGQING620
 * @date 2020-07-27 19:52
 */
@Order(5)
@Aspect
@Component
@Slf4j
public class RedisLockAdvice {

    @Pointcut("@annotation(com.minister.component.redis.distributedlock.aop.RedisLock)")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
    }

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        RedisLock redisLock = AopUtil.findAnnotation(proceedingJoinPoint, RedisLock.class);

        Object result = null;
        if (RedisSimpleLockUtil.class.equals(redisLock.lockClass())) {
            RedisSimpleLockUtil redisLockUtil = RedisSimpleLockUtil.init(redisLock.key(), redisLock.lockTime());
            try {
                if (redisLock.isWait() ?
                        redisLockUtil.tryLock(redisLock.waitTime(), TimeUnit.SECONDS) :
                        redisLockUtil.tryLock()) {
                    result = proceedingJoinPoint.proceed();
                }
            } finally {
                redisLockUtil.unlock();
            }
        } else if (RedisLocalLockUtil.class.equals(redisLock.lockClass())) {
            RedisLocalLockUtil redisLockUtil = RedisLocalLockUtil.init(redisLock.key(), redisLock.lockTime());
            try {
                if (redisLock.isWait() ?
                        redisLockUtil.tryLock(redisLock.waitTime(), TimeUnit.SECONDS) :
                        redisLockUtil.tryLock()) {
                    result = proceedingJoinPoint.proceed();
                }
            } finally {
                redisLockUtil.unlock();
            }
        } else {
            log.error("can not find lock class");
        }

        return result;
    }

    @After("pointcut()")
    public void doAfter(JoinPoint joinPoint) throws Throwable {
    }

}
