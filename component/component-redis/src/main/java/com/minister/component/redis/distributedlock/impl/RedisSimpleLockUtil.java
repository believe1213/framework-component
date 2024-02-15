package com.minister.component.redis.distributedlock.impl;

import cn.hutool.core.util.IdUtil;
import com.minister.component.redis.distributedlock.RedisLockUtil;
import com.minister.component.utils.StrUtil;
import com.minister.component.utils.context.ThreadLocalContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static cn.hutool.core.text.CharPool.COLON;
import static com.minister.component.redis.constants.RedisLockConstants.*;
import static com.minister.component.utils.constants.Constants.SUCCESS_LOW;
import static com.minister.component.utils.constants.Constants.TIMEOUT_LOW;

/**
 * 纯redis分布式锁Util
 *
 * @author QIUCHANGQING620
 * @date 2020-07-28 19:13
 */
@Component
@Slf4j
public class RedisSimpleLockUtil implements InitializingBean, RedisLockUtil {

    private RedisSimpleLockUtil() {
    }

    /**
     * 获取锁
     */
    private static final String LOCK_SCRIPT =
            "if redis.call('GET', KEYS[1]) == ARGV[1] then\n" +
                    "   redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))\n" +
                    "   return true\n" +
                    "elseif redis.call('SETNX', KEYS[1], ARGV[1]) == 1 then\n" +
                    "   redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))\n" +
                    "   return true\n" +
                    "else\n" +
                    "   return false\n" +
                    "end";

    /**
     * 释放锁
     */
    private static final String UNLOCK_SCRIPT =
            "if redis.call('GET', KEYS[1]) == ARGV[1] then\n" +
                    "   redis.call('DEL', KEYS[1])\n" +
                    "   return true\n" +
                    "else\n" +
                    "   return false\n" +
                    "end";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static StringRedisTemplate redisTemplate;

    /**
     * 锁名(redis中存放的锁全名为 {LOCK_PREFIX}:{lockKey})
     */
    private String lockKey;

    /**
     * redis中存储时间(s)
     */
    private long expireAfter;

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
        RedisSimpleLockUtil.redisTemplate = stringRedisTemplate;
    }

    private RedisSimpleLockUtil(String lockKey, long expireAfter) {
        if (StringUtils.isBlank(lockKey)) {
            throw new IllegalArgumentException("lockKey can not be null");
        }
        this.lockKey = lockKey;
        this.expireAfter = expireAfter;
        ThreadLocalContext.put(LOCK_TAG_KEY_PREFIX + lockKey, this.tag);
    }

    /**
     * 实例化分布式锁
     *
     * @param lockKey     锁名(redis中存放的锁全名为 {LOCK_PREFIX}:{lockKey})
     * @param expireAfter redis中存储时间(s)
     * @return 纯redis分布式锁Util
     */
    public static RedisSimpleLockUtil init(String lockKey, long expireAfter) {
        if (StringUtils.isBlank(lockKey)) {
            throw new IllegalArgumentException("lockKey can not be null");
        }
        return new RedisSimpleLockUtil(lockKey, expireAfter);
    }

    /**
     * 实例化分布式锁
     *
     * @param lockKey 锁名(redis中存放的锁全名为 {LOCK_PREFIX}:{lockKey})
     * @return 纯redis分布式锁Util
     */
    public static RedisSimpleLockUtil init(String lockKey) {
        return init(lockKey, LOCK_TIME);
    }

    /**
     * 无限时间尝试上锁直至成功（不建议使用）
     */
    @Override
    public void lock() {
        checkTag();
        while (true) {
            try {
                while (!obtainLock()) {
                    Thread.sleep(100);
                }
                break;
            } catch (InterruptedException e) {
                /*
                 * This method must be uninterruptible so catch and ignore
                 * interrupts and only break out of the while loop when
                 * we get the lock.
                 */
            }
        }

        this.isLocked = true;
        log.info("lock [{}] success", this.lockKey);
    }

    /**
     * 尝试上锁
     *
     * @return 锁状态
     */
    @Override
    public boolean tryLock() {
        return tryLock(0, TimeUnit.MILLISECONDS);
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
            long now = System.currentTimeMillis();
            long expire = now + TimeUnit.MILLISECONDS.convert(time, unit);
            while (!(result = obtainLock()) && System.currentTimeMillis() < expire) {
                Thread.sleep(100);
            }

            if (result) {
                this.isLocked = true;
            }
            log.info("tryLock [{}] {}", this.lockKey, result ? SUCCESS_LOW : TIMEOUT_LOW);
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

        boolean result = releaseLock();
        this.isLocked = false;
        if (result) {
            log.info("unlock [{}] {}", this.lockKey, SUCCESS_LOW);
            ThreadLocalContext.remove(LOCK_TAG_KEY_PREFIX + this.lockKey);
        } else {
            log.info("key [{}] was released", this.lockKey);
        }
    }

    private boolean obtainLock() {
        DefaultRedisScript<Boolean> rs = new DefaultRedisScript<>(LOCK_SCRIPT, Boolean.class);
        List<String> keysList = new ArrayList<>();
        keysList.add(LOCK_PREFIX + COLON + this.lockKey);
        Object[] argvList = new Object[]{this.tag, String.valueOf(this.expireAfter)};
        Boolean isSuccess = RedisSimpleLockUtil.redisTemplate.execute(rs, keysList, argvList);

        return Boolean.TRUE.equals(isSuccess);
    }

    private boolean releaseLock() {
        DefaultRedisScript<Boolean> rs = new DefaultRedisScript<>(UNLOCK_SCRIPT, Boolean.class);
        List<String> keysList = new ArrayList<>();
        keysList.add(LOCK_PREFIX + COLON + this.lockKey);
        Object[] argvList = new Object[]{this.tag};
        Boolean isSuccess = RedisSimpleLockUtil.redisTemplate.execute(rs, keysList, argvList);

        return Boolean.TRUE.equals(isSuccess);
    }

    private void checkTag() {
        String tag = ThreadLocalContext.get(LOCK_TAG_KEY_PREFIX + this.lockKey);
        if (Objects.isNull(tag) || !StrUtil.equals(this.tag, tag)) {
            throw new IllegalThreadStateException(this.getClass().getSimpleName() + " can not run in different thread");
        }
    }

}
