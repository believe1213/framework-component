package com.minister.framework.boot.config;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.minister.component.trace.utils.ThreadPoolUtil;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;

import java.util.concurrent.*;

/**
 * 配置 @Async
 *
 * @author QIUCHANGQING620
 * @date 2020-03-07 23:41
 */
@Configuration
public class AsyncConfigurer extends AsyncConfigurerSupport {

    private static final String PREFIX = "async-";

    @Value("${framework-boot.async.core-pool-size:100}")
    private Integer corePoolSize;

    @Value("${framework-boot.async.maximum-pool-size:500}")
    private Integer maximumPoolSize;

    @Value("${framework-boot.async.keep-alive-time:180}")
    private Long keepAliveTime;

    @Value("${framework-boot.async.capacity:100}")
    private Integer capacity;

    @Override
    public Executor getAsyncExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNamePrefix(PREFIX).build();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolUtil.ThreadPoolExecutorMDCWrapper(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(capacity), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());

        return TtlExecutors.getTtlExecutorService(threadPoolExecutor);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return super.getAsyncUncaughtExceptionHandler();
    }

}
