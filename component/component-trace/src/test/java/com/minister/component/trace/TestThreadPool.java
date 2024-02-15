package com.minister.component.trace;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.util.RuntimeUtil;
import com.google.common.collect.Lists;
import com.minister.component.trace.utils.ThreadPoolUtil;
import com.minister.component.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.*;

/**
 * TestThreadPool
 *
 * @author QIUCHANGQING620
 * @date 2020-07-27 15:36
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestTraceApplication.class)
@Slf4j
public class TestThreadPool {

    private static final int CPU_CORE_COUNT = RuntimeUtil.getProcessorCount();

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setNamePrefix("fileMate-").build();

    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolUtil.ThreadPoolExecutorMDCWrapper(
            CPU_CORE_COUNT, CPU_CORE_COUNT * 4, 180, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(CPU_CORE_COUNT * 128), THREAD_FACTORY, new ThreadPoolExecutor.CallerRunsPolicy());

    @Test
    public void t1() {
        List<CompletableFuture<String>> futureList = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            final int fi = i;
            CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> process(fi), THREAD_POOL_EXECUTOR);
            futureList.add(f);
        }
        List<String> result = Lists.newArrayList();
        for (CompletableFuture<String> future : futureList) {
            try {
                result.add(future.get());
            } catch (Exception e) {
                log.error("future get fail.", e);
            }
        }
        log.info("result : {}", JacksonUtil.bean2Json(result));
    }

    private String process(int i) {
        log.info("t{} process", i);
        return String.valueOf(i);
    }

}
