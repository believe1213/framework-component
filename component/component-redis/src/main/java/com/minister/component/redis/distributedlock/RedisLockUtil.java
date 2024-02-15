package com.minister.component.redis.distributedlock;

import java.util.concurrent.TimeUnit;

/**
 * redis锁
 *
 * @author QIUCHANGQING
 * @date 2024-02-13 16:28
 */
public interface RedisLockUtil {

    void lock();

    boolean tryLock();

    boolean tryLock(long time, TimeUnit unit);

    void unlock();

}
