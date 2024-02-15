package com.minister.component.trace.utils;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.minister.component.trace.constants.TraceConstants;
import com.minister.component.trace.context.TraceContext;
import com.minister.component.utils.context.HeadersContext;
import com.minister.component.utils.context.ThreadLocalContext;
import com.minister.component.utils.entity.HeaderEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 线程池工具类
 *
 * @author QIUCHANGQING620
 * @date 2020-03-08 16:25
 */
public class ThreadPoolUtil {

    /**
     * 设置链路跟踪参数
     *
     * @param useParent 是否使用过父线程Context
     */
    private static void setTraceIfAbsent(boolean useParent) {
        // 线程跟踪id每次都必须重新生成
        MDC.put(TraceConstants.THREAD_ID, TraceContext.initThreadId());

        if (useParent) {
            String traceId = TraceContext.getTraceId();
            if (StringUtils.isBlank(traceId)) {
                traceId = TraceContext.initTraceId();
            }
            MDC.put(TraceConstants.TRACE_ID, traceId);

            HeaderEntity headerEntity = HeadersContext.getHeaderEntity();
            String userId = headerEntity.getUserId();
            if (StringUtils.isNotBlank(userId)) {
                MDC.put(TraceConstants.USER_ID, userId);
            }

            String batchId = headerEntity.getBatchId();
            if (StringUtils.isNotBlank(batchId)) {
                MDC.put(TraceConstants.BATCH_ID, batchId);
            }
        } else {
            String traceId = TraceContext.initTraceId();
            MDC.put(TraceConstants.TRACE_ID, traceId);
        }
    }

    private static void clearMDC() {
        MDC.remove(TraceConstants.TRACE_ID);
        MDC.remove(TraceConstants.THREAD_ID);
        MDC.remove(TraceConstants.USER_ID);
        MDC.remove(TraceConstants.BATCH_ID);
    }

    private static void clearContext() {
        HeadersContext.clean();
        TraceContext.clean();
        ThreadLocalContext.clean();
    }

    public static <T> Callable<T> wrap(final Callable<T> callable) {
        return () -> {
            setTraceIfAbsent(true);
            try {
                return callable.call();
            } finally {
                clearMDC();
                clearContext();
            }
        };
    }

    public static Runnable wrap(final Runnable runnable) {
        return () -> {
            setTraceIfAbsent(true);
            try {
                runnable.run();
            } finally {
                clearMDC();
                clearContext();
            }
        };
    }

    public static <T> Callable<T> wrapScheduler(final Callable<T> callable) {
        return () -> {
            setTraceIfAbsent(false);
            try {
                return callable.call();
            } finally {
                clearMDC();
                clearContext();
            }
        };
    }

    public static Runnable wrapScheduler(final Runnable runnable) {
        return () -> {
            setTraceIfAbsent(false);
            try {
                runnable.run();
            } finally {
                clearMDC();
                clearContext();
            }
        };
    }

    // ========== Thread =========

    public static class ThreadMDCWrapper extends Thread {
        public ThreadMDCWrapper(Runnable task, String name) {
            super(ThreadPoolUtil.wrap(task), name);
        }
    }

    // ========== java.util.concurrent =========

    /**
     * 使用时请使用{@link TtlExecutors}包装
     */
    public static class ThreadPoolExecutorMDCWrapper extends ThreadPoolExecutor {
        public ThreadPoolExecutorMDCWrapper(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        public ThreadPoolExecutorMDCWrapper(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }

        public ThreadPoolExecutorMDCWrapper(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        }

        public ThreadPoolExecutorMDCWrapper(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }

        @Override
        public void execute(Runnable task) {
            super.execute(ThreadPoolUtil.wrap(task));
        }

        @Override
        protected void beforeExecute(Thread t, Runnable task) {
            super.beforeExecute(t, ThreadPoolUtil.wrap(task));
        }

        @Override
        protected void afterExecute(Runnable task, Throwable t) {
            super.afterExecute(ThreadPoolUtil.wrap(task), t);
        }

        @Override
        protected <T> RunnableFuture<T> newTaskFor(Runnable task, T value) {
            return super.newTaskFor(ThreadPoolUtil.wrap(task), value);
        }

        @Override
        protected <T> RunnableFuture<T> newTaskFor(Callable<T> task) {
            return super.newTaskFor(ThreadPoolUtil.wrap(task));
        }

        @Override
        @NonNull
        public Future<?> submit(Runnable task) {
            return super.submit(ThreadPoolUtil.wrap(task));
        }

        @Override
        @NonNull
        public <T> Future<T> submit(Runnable task, T result) {
            return super.submit(ThreadPoolUtil.wrap(task), result);
        }

        @Override
        @NonNull
        public <T> Future<T> submit(Callable<T> task) {
            return super.submit(ThreadPoolUtil.wrap(task));
        }

        @Override
        @NonNull
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            tasks = tasks.stream().map(ThreadPoolUtil::wrap).collect(Collectors.toList());

            return super.invokeAny(tasks);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            tasks = tasks.stream().map(ThreadPoolUtil::wrap).collect(Collectors.toList());

            return super.invokeAny(tasks, timeout, unit);
        }

        @Override
        @NonNull
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            tasks = tasks.stream().map(ThreadPoolUtil::wrap).collect(Collectors.toList());

            return super.invokeAll(tasks);
        }

        @Override
        @NonNull
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            tasks = tasks.stream().map(ThreadPoolUtil::wrap).collect(Collectors.toList());

            return super.invokeAll(tasks, timeout, unit);
        }
    }

    /**
     * 使用时请使用{@link TtlExecutors}包装
     */
    public static class ScheduledThreadPoolExecutorMDCWrapper extends ScheduledThreadPoolExecutor {
        public ScheduledThreadPoolExecutorMDCWrapper(int corePoolSize) {
            super(corePoolSize);
        }

        public ScheduledThreadPoolExecutorMDCWrapper(int corePoolSize, ThreadFactory threadFactory) {
            super(corePoolSize, threadFactory);
        }

        public ScheduledThreadPoolExecutorMDCWrapper(int corePoolSize, RejectedExecutionHandler handler) {
            super(corePoolSize, handler);
        }

        public ScheduledThreadPoolExecutorMDCWrapper(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
            super(corePoolSize, threadFactory, handler);
        }

        @Override
        protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
            return super.decorateTask(ThreadPoolUtil.wrapScheduler(runnable), task);
        }

        @Override
        protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
            return super.decorateTask(ThreadPoolUtil.wrapScheduler(callable), task);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
            return super.schedule(ThreadPoolUtil.wrapScheduler(task), delay, unit);
        }

        @Override
        @NonNull
        public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
            return super.schedule(ThreadPoolUtil.wrapScheduler(task), delay, unit);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
            return super.scheduleAtFixedRate(ThreadPoolUtil.wrapScheduler(task), initialDelay, period, unit);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
            return super.scheduleWithFixedDelay(ThreadPoolUtil.wrapScheduler(task), initialDelay, delay, unit);
        }

        @Override
        public void execute(Runnable task) {
            super.execute(ThreadPoolUtil.wrapScheduler(task));
        }

        @Override
        @NonNull
        public Future<?> submit(Runnable task) {
            return super.submit(ThreadPoolUtil.wrapScheduler(task));
        }

        @Override
        @NonNull
        public <T> Future<T> submit(Runnable task, T result) {
            return super.submit(ThreadPoolUtil.wrapScheduler(task), result);
        }

        @Override
        @NonNull
        public <T> Future<T> submit(Callable<T> task) {
            return super.submit(ThreadPoolUtil.wrapScheduler(task));
        }

        @Override
        protected void beforeExecute(Thread t, Runnable task) {
            super.beforeExecute(t, ThreadPoolUtil.wrapScheduler(task));
        }

        @Override
        protected void afterExecute(Runnable task, Throwable t) {
            super.afterExecute(ThreadPoolUtil.wrapScheduler(task), t);
        }

        @Override
        protected <T> RunnableFuture<T> newTaskFor(Runnable task, T value) {
            return super.newTaskFor(ThreadPoolUtil.wrapScheduler(task), value);
        }

        @Override
        protected <T> RunnableFuture<T> newTaskFor(Callable<T> task) {
            return super.newTaskFor(ThreadPoolUtil.wrapScheduler(task));
        }

        @Override
        @NonNull
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            tasks = tasks.stream().map(ThreadPoolUtil::wrapScheduler).collect(Collectors.toList());

            return super.invokeAny(tasks);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            tasks = tasks.stream().map(ThreadPoolUtil::wrapScheduler).collect(Collectors.toList());

            return super.invokeAny(tasks, timeout, unit);
        }

        @Override
        @NonNull
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            tasks = tasks.stream().map(ThreadPoolUtil::wrapScheduler).collect(Collectors.toList());

            return super.invokeAll(tasks);
        }

        @Override
        @NonNull
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            tasks = tasks.stream().map(ThreadPoolUtil::wrapScheduler).collect(Collectors.toList());

            return super.invokeAll(tasks, timeout, unit);
        }
    }

    // ========== org.springframework.scheduling.concurrent =========

    public static class ThreadPoolTaskExecutorMDCWrapper extends ThreadPoolTaskExecutor {
        @Override
        protected ExecutorService initializeExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
            return TtlExecutors.getTtlExecutorService(super.initializeExecutor(threadFactory, rejectedExecutionHandler));
        }

        @Override
        public void execute(@NonNull Runnable task) {
            super.execute(ThreadPoolUtil.wrap(task));
        }

        @Override
        public void execute(@NonNull Runnable task, long startTimeout) {
            super.execute(ThreadPoolUtil.wrap(task), startTimeout);
        }

        @Override
        @NonNull
        public <T> Future<T> submit(@NonNull Callable<T> task) {
            return super.submit(ThreadPoolUtil.wrap(task));
        }

        @Override
        @NonNull
        public Future<?> submit(@NonNull Runnable task) {
            return super.submit(ThreadPoolUtil.wrap(task));
        }

        @Override
        @NonNull
        public ListenableFuture<?> submitListenable(Runnable task) {
            return super.submitListenable(ThreadPoolUtil.wrap(task));
        }

        @Override
        @NonNull
        public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
            return super.submitListenable(ThreadPoolUtil.wrap(task));
        }

        @Override
        @NonNull
        public Thread createThread(@NonNull Runnable task) {
            return super.createThread(ThreadPoolUtil.wrap(task));
        }
    }

    public static class ThreadPoolTaskSchedulerMDCWrapper extends ThreadPoolTaskScheduler {
        @Override
        protected ExecutorService initializeExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
            return TtlExecutors.getTtlExecutorService(super.initializeExecutor(threadFactory, rejectedExecutionHandler));
        }

        @Override
        public void execute(Runnable task) {
            super.execute(ThreadPoolUtil.wrapScheduler(task));
        }

        @Override
        public void execute(@NonNull Runnable task, long startTimeout) {
            super.execute(ThreadPoolUtil.wrapScheduler(task), startTimeout);
        }

        @Override
        @NonNull
        public Future<?> submit(Runnable task) {
            return super.submit(ThreadPoolUtil.wrapScheduler(task));
        }

        @Override
        @NonNull
        public <T> Future<T> submit(@NonNull Callable<T> task) {
            return super.submit(ThreadPoolUtil.wrapScheduler(task));
        }

        @Override
        @NonNull
        public ListenableFuture<?> submitListenable(Runnable task) {
            return super.submitListenable(ThreadPoolUtil.wrapScheduler(task));
        }

        @Override
        @NonNull
        public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
            return super.submitListenable(ThreadPoolUtil.wrapScheduler(task));
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, @NonNull Trigger trigger) {
            return super.schedule(ThreadPoolUtil.wrapScheduler(task), trigger);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
            return super.schedule(ThreadPoolUtil.wrapScheduler(task), startTime);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
            return super.scheduleAtFixedRate(ThreadPoolUtil.wrapScheduler(task), startTime, period);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
            return super.scheduleAtFixedRate(ThreadPoolUtil.wrapScheduler(task), period);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
            return super.scheduleWithFixedDelay(ThreadPoolUtil.wrapScheduler(task), startTime, delay);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
            return super.scheduleWithFixedDelay(ThreadPoolUtil.wrapScheduler(task), delay);
        }

        @Override
        @NonNull
        public Thread createThread(@NonNull Runnable task) {
            return super.createThread(ThreadPoolUtil.wrapScheduler(task));
        }
    }

    // ========== ForkJoinPool =========
    // TODO ForkJoinPool使用优化

    public static class ForkJoinPoolMDCWrapper extends ForkJoinPool {
        public ForkJoinPoolMDCWrapper() {
            super();
        }

        public ForkJoinPoolMDCWrapper(int parallelism) {
            super(parallelism);
        }

        public ForkJoinPoolMDCWrapper(int parallelism, ForkJoinWorkerThreadFactory factory, Thread.UncaughtExceptionHandler handler, boolean asyncMode) {
            super(parallelism, factory, handler, asyncMode);
        }

        @Override
        public <T> T invoke(ForkJoinTask<T> task) {
            setTraceIfAbsent(true);
            try {
                return super.invoke(task);
            } finally {
                clearMDC();
                clearContext();
            }
        }

        @Override
        public void execute(ForkJoinTask<?> task) {
            setTraceIfAbsent(true);
            try {
                super.execute(task);
            } finally {
                clearMDC();
                clearContext();
            }
        }

        @Override
        public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
            setTraceIfAbsent(true);
            try {
                return super.submit(task);
            } finally {
                clearMDC();
                clearContext();
            }
        }

        @Override
        public void execute(Runnable task) {
            super.execute(ThreadPoolUtil.wrap(task));
        }

        @Override
        @NonNull
        public <T> ForkJoinTask<T> submit(Callable<T> task) {
            return super.submit(ThreadPoolUtil.wrap(task));
        }

        @Override
        @NonNull
        public <T> ForkJoinTask<T> submit(Runnable task, T result) {
            return super.submit(ThreadPoolUtil.wrap(task), result);
        }

        @Override
        @NonNull
        public ForkJoinTask<?> submit(Runnable task) {
            return super.submit(ThreadPoolUtil.wrap(task));
        }

        @Override
        protected <T> RunnableFuture<T> newTaskFor(Runnable task, T value) {
            return super.newTaskFor(ThreadPoolUtil.wrap(task), value);
        }

        @Override
        protected <T> RunnableFuture<T> newTaskFor(Callable<T> task) {
            return super.newTaskFor(ThreadPoolUtil.wrap(task));
        }

        @Override
        @NonNull
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            tasks = tasks.stream().map(ThreadPoolUtil::wrap).collect(Collectors.toList());

            return super.invokeAny(tasks);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            tasks = tasks.stream().map(ThreadPoolUtil::wrap).collect(Collectors.toList());

            return super.invokeAny(tasks, timeout, unit);
        }

        @Override
        @NonNull
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            tasks = tasks.stream().map(ThreadPoolUtil::wrap).collect(Collectors.toList());

            return super.invokeAll(tasks, timeout, unit);
        }

        @Override
        @NonNull
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
            tasks = tasks.stream().map(ThreadPoolUtil::wrap).collect(Collectors.toList());

            return super.invokeAll(tasks);
        }
    }

    /**
     * ForkJoinTask链路跟踪需设置jvm参数，也可在main方法类中添加如下代码
     * <pre> {@code
     * static {
     *   System.setProperty("java.util.concurrent.ForkJoinPool.common.threadFactory",
     *     ThreadPoolUtil.ForkJoinWorkerThreadFactoryMDCWrapper.class.getName());
     * }}</pre>
     * ForkJoinTask在lambda中parallel()被使用，也在ForkJoin执行时被调用
     */
    public static class ForkJoinWorkerThreadFactoryMDCWrapper implements ForkJoinPool.ForkJoinWorkerThreadFactory {
        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new ForkJoinWorkerThreadMDCWrapper(pool);
        }
    }

    public static class ForkJoinWorkerThreadMDCWrapper extends ForkJoinWorkerThread {
        public ForkJoinWorkerThreadMDCWrapper(ForkJoinPool pool) {
            super(pool);
        }

        @Override
        public void run() {
            // TODO setTraceIfAbsent
            try {
                super.run();
            } finally {
                clearMDC();
                clearContext();
            }
        }
    }

}
