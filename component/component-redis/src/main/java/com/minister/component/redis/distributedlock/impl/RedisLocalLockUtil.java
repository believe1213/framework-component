package com.minister.component.redis.distributedlock.impl;

import cn.hutool.core.util.IdUtil;
import com.minister.component.redis.distributedlock.RedisLockUtil;
import com.minister.component.utils.StrUtil;
import com.minister.component.utils.context.ThreadLocalContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static com.minister.component.redis.constants.RedisLockConstants.*;
import static com.minister.component.utils.constants.Constants.*;

/**
 * 本地锁 + 分布式锁Util
 *
 * @author QIUCHANGQING620
 * @date 2020-07-27 19:13
 */
@Component
@Slf4j
public class RedisLocalLockUtil implements InitializingBean, RedisLockUtil {

    private RedisLocalLockUtil() {
    }

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    private static RedisConnectionFactory rcFactory;

    /**
     * 锁
     */
    private Lock lock;

    /**
     * 锁名(redis中存放的锁全名为 {LOCK_PREFIX}:{lockKey})
     */
    private String lockKey;

    /**
     * 上锁状态
     */
    private boolean isLocked;

    /**
     * 唯一标签
     */
    private final String tag = IdUtil.fastSimpleUUID();

    @Override
    public void afterPropertiesSet() throws Exception {
        RedisLocalLockUtil.rcFactory = redisConnectionFactory;
    }

    private RedisLocalLockUtil(String lockKey, long expireAfter, TimeUnit unit) {
        if (StringUtils.isBlank(lockKey)) {
            throw new IllegalArgumentException("lockKey can not be null");
        }
        this.lockKey = lockKey;
        this.lock = new RedisLockRegistry(rcFactory, LOCK_PREFIX, TimeUnit.MILLISECONDS.convert(expireAfter, unit)).obtain(lockKey);
        ThreadLocalContext.put(LOCK_TAG_KEY_PREFIX + lockKey, this.tag);
    }

    /**
     * 实例化分布式锁
     * <p>
     * redis版本号 > 4
     * 请勿在不同线程之间传递和使用分布式锁
     * </p>
     *
     * @param lockKey     锁名(redis中存放的锁全名为 {LOCK_PREFIX}:{lockKey})
     * @param expireAfter redis中存储时间(本地锁时间应小于redis锁时间)
     * @param unit        时间单位
     * @return 分布式锁Util
     */
    public static RedisLocalLockUtil init(String lockKey, long expireAfter, TimeUnit unit) {
        return new RedisLocalLockUtil(lockKey, expireAfter, unit);
    }

    /**
     * 实例化分布式锁
     * <p>
     * redis版本号 > 4
     * 请勿在不同线程之间传递和使用分布式锁
     * </p>
     *
     * @param lockKey     锁名(redis中存放的锁全名为 {LOCK_PREFIX}:{lockKey})
     * @param expireAfter redis中存储时间(s, 本地锁时间应小于redis锁时间)
     * @return 分布式锁Util
     */
    public static RedisLocalLockUtil init(String lockKey, long expireAfter) {
        return new RedisLocalLockUtil(lockKey, expireAfter, TimeUnit.SECONDS);
    }

    /**
     * 实例化分布式锁
     * <p>
     * redis版本号 > 4
     * 请勿在不同线程之间传递和使用分布式锁
     * </p>
     *
     * @param lockKey 锁名(redis中存放的锁全名为 {LOCK_PREFIX}:{lockKey})
     * @return 分布式锁Util
     */
    public static RedisLocalLockUtil init(String lockKey) {
        return init(lockKey, LOCK_TIME);
    }

    /**
     * 无限时间尝试上锁直至成功（不建议使用）
     */
    @Override
    public void lock() {
        checkTag();
        this.lock.lock();
        this.isLocked = true;
        log.info("lock [{}] success", this.lockKey);
    }

    /**
     * 无限时间尝试上锁直至成功或线程中断（不建议使用）
     */
    public void lockInterruptibly() {
        checkTag();
        try {
            this.lock.lockInterruptibly();
            this.isLocked = true;
            log.info("lockInterruptibly [{}] success", this.lockKey);
        } catch (InterruptedException e) {
            log.info("lockInterruptibly [{}] was interrupted", this.lockKey);
        }
    }

    /**
     * 尝试上锁
     *
     * @return 锁状态
     */
    @Override
    public boolean tryLock() {
        checkTag();
        boolean result = false;
        try {
            result = this.lock.tryLock();
            if (result) {
                this.isLocked = true;
            }
            log.info("tryLock [{}] {}", this.lockKey, result ? SUCCESS_LOW : FAIL_LOW);
        } catch (IllegalStateException e) {
            log.info("key [{}] was locked", this.lockKey);
        }
        return result;
    }

    /**
     * 尝试上锁(尝试时间内不断尝试上锁)
     *
     * @param time 尝试时间
     * @param unit 尝试时间单位
     * @return 锁状态
     */
    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        checkTag();
        boolean result = false;
        try {
            result = this.lock.tryLock(time, unit);
            if (result) {
                this.isLocked = true;
            }
            log.info("tryLock key [{}] {}", this.lockKey, result ? SUCCESS_LOW : TIMEOUT_LOW);
        } catch (IllegalStateException e) {
            log.info("key [{}] was locked", this.lockKey);
        } catch (InterruptedException e) {
            log.info("tryLock [{}] was interrupted", this.lockKey);
        }
        return result;
    }

    @Override
    public void unlock() {
        checkTag();
        if (!isLocked) {
            log.info("key [{}] did not exists", this.lockKey);
            return;
        }
        try {
            this.lock.unlock();
            this.isLocked = false;
            log.info("unlock [{}] {}", this.lockKey, SUCCESS_LOW);
            ThreadLocalContext.remove(LOCK_TAG_KEY_PREFIX + this.lockKey);
        } catch (IllegalStateException e) {
            log.info("key [{}] was released", this.lockKey);
        }
    }

    private void checkTag() {
        String tag = ThreadLocalContext.get(LOCK_TAG_KEY_PREFIX + this.lockKey);
        if (Objects.isNull(tag) || !StrUtil.equals(this.tag, tag)) {
            throw new IllegalThreadStateException(this.getClass().getSimpleName() + " can not run in different thread");
        }
    }

}
