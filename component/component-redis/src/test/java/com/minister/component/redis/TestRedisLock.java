package com.minister.component.redis;

import com.minister.component.redis.distributedlock.impl.RedisLocalLockUtil;
import com.minister.component.redis.distributedlock.impl.RedisSimpleLockUtil;
import com.minister.component.redis.service.RedisStringService;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * TestIntegration
 *
 * @author QIUCHANGQING620
 * @date 2020-07-27 15:36
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestRedisApplication.class)
@Slf4j
public class TestRedisLock {

    @Resource
    private RedisStringService redisStringService;

    @Before
    public void setUp() {
        redisStringService.del("lockKey:lockKey");
    }

    @After
    public void tearDown() {
        redisStringService.del("lockKey:lockKey");
    }

    @Test
    public void t1() {
        RedisLocalLockUtil redisLocalLockUtil = RedisLocalLockUtil.init("lockKey", 5);
        try {
            boolean result = redisLocalLockUtil.tryLock(10, TimeUnit.SECONDS);
            log.info("result : " + result);
            Assert.assertTrue(result);

            Thread t = new Thread(() -> {
                log.info("new thread1 start");
                RedisLocalLockUtil redisLocalLockUtil1 = RedisLocalLockUtil.init("lockKey", 5);
                boolean result1 = redisLocalLockUtil1.tryLock();
                log.info("result1 : " + result1);
                Assert.assertFalse(result1);
                log.info("new thread1 end");
                redisLocalLockUtil1.unlock();
            });
            t.start();

            Thread t2 = new Thread(() -> {
                log.info("new thread2 start");
                RedisLocalLockUtil redisLocalLockUtil2 = RedisLocalLockUtil.init("lockKey", 5);
                boolean result2 = redisLocalLockUtil2.tryLock(3, TimeUnit.SECONDS);
                log.info("result2 : " + result2);
                Assert.assertFalse(result2);
                log.info("new thread2 end");
                redisLocalLockUtil2.unlock();
            });
            t2.start();

            Thread t3 = new Thread(() -> {
                log.info("new thread3 start");
                RedisLocalLockUtil redisLocalLockUtil3 = RedisLocalLockUtil.init("lockKey", 5);
                boolean result3 = redisLocalLockUtil3.tryLock(6, TimeUnit.SECONDS);
                log.info("result3 : " + result3);
                Assert.assertTrue(result3);
                log.info("new thread3 end");
                redisLocalLockUtil3.unlock();
            });
            t3.start();

            try {
                TimeUnit.SECONDS.sleep(7);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            redisLocalLockUtil.unlock();
        }
    }

    @Test
    public void t2() {
        RedisLocalLockUtil redisLocalLockUtil = RedisLocalLockUtil.init("lockKey", 3);
        redisLocalLockUtil.lock();
        redisLocalLockUtil.unlock();
    }

    @Test
    public void t3() {
        RedisSimpleLockUtil redisSimpleLockUtil = RedisSimpleLockUtil.init("lockKey", 10);
        redisSimpleLockUtil.tryLock();
        redisSimpleLockUtil.unlock();
    }

}
