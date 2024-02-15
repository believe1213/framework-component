package com.minister.component.redis.constants;

import java.util.concurrent.TimeUnit;

/**
 * RedisConstants
 *
 * @author QIUCHANGQING620
 * @date 2020-07-28 15:30
 */
public interface RedisLockConstants {

    /**
     * 分布式锁 key 前缀
     */
    String LOCK_PREFIX = "lock";

    /**
     * 默认分布式锁时间
     */
    long LOCK_TIME = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);

    /**
     * 线程标记Key
     */
    String LOCK_TAG_KEY_PREFIX = "redis_lock_key_";

}
