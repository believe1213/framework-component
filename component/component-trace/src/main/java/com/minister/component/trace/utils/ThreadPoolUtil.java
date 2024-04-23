package com.minister.component.trace.utils;

import com.google.common.collect.Maps;
import com.minister.component.trace.constants.TraceConstants;
import com.minister.component.trace.context.TraceContext;
import com.minister.component.trace.entity.TraceEntity;
import com.minister.component.utils.context.HeadersContext;
import com.minister.component.utils.context.ThreadLocalContext;
import com.minister.component.utils.entity.HeaderEntity;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 线程池工具类
 *
 * @author QIUCHANGQING620
 * @date 2020-03-08 16:25
 */
public class ThreadPoolUtil {

    private static void initThreadId() {
        // 线程跟踪id每次都必须重新生成
        MDC.put(TraceConstants.THREAD_ID, TraceContext.initThreadId());
    }

    private static void putMDCUserId(String userId) {
        if (Objects.isNull(userId)) {
            MDC.remove(TraceConstants.USER_ID);
        } else {
            MDC.put(TraceConstants.USER_ID, userId);
        }
    }

    private static void putMDCBatchId(String batchId) {
        if (Objects.isNull(batchId)) {
            MDC.remove(TraceConstants.BATCH_ID);
        } else {
            MDC.put(TraceConstants.BATCH_ID, batchId);
        }
    }

    private static void putMDCChainId(String chainId) {
        if (Objects.isNull(chainId)) {
            MDC.remove(TraceConstants.CHAIN_ID);
        } else {
            MDC.put(TraceConstants.CHAIN_ID, chainId);
        }
    }

    private static void putMDCNodeId(String nodeId) {
        if (Objects.isNull(nodeId)) {
            MDC.remove(TraceConstants.NODE_ID);
        } else {
            MDC.put(TraceConstants.NODE_ID, nodeId);
        }
    }

    private static void clearContext() {
        HeadersContext.clean();
        TraceContext.cleanAll();
        ThreadLocalContext.clean();
    }

    private static void setTrace(HeaderEntity headerEntity, TraceEntity traceEntity, Map<String, Object> threadLocal) {
        if (Objects.isNull(headerEntity)) {
            headerEntity = new HeaderEntity();
        }
        if (Objects.isNull(traceEntity)) {
            traceEntity = new TraceEntity();
        }
        if (MapUtils.isEmpty(threadLocal)) {
            threadLocal = Maps.newConcurrentMap();
        }
        HeadersContext.setHeaderEntity(headerEntity);
        TraceContext.set(traceEntity);
        ThreadLocalContext.set(threadLocal);

        String traceId = traceEntity.getTraceId();
        if (StringUtils.isBlank(traceId)) {
            traceId = TraceContext.initTraceId();
        }
        MDC.put(TraceConstants.TRACE_ID, traceId);

        initThreadId();

        putMDCUserId(headerEntity.getUserId());
        putMDCBatchId(headerEntity.getBatchId());
        putMDCChainId(traceEntity.getChainId());
        putMDCNodeId(traceEntity.getNodeId());
    }

    public static <T> Callable<T> wrap(final Callable<T> callable) {
        HeaderEntity headerEntity = HeadersContext.copy();
        TraceEntity traceEntity = TraceContext.copy();
        Map<String, Object> threadLocal = ThreadLocalContext.copy();
        return () -> {
            setTrace(headerEntity, traceEntity, threadLocal);
            try {
                return callable.call();
            } finally {
                MDC.clear();
                clearContext();
            }
        };
    }

    public static Runnable wrap(final Runnable runnable) {
        HeaderEntity headerEntity = HeadersContext.copy();
        TraceEntity traceEntity = TraceContext.copy();
        Map<String, Object> threadLocal = ThreadLocalContext.copy();
        return () -> {
            setTrace(headerEntity, traceEntity, threadLocal);
            try {
                runnable.run();
            } finally {
                MDC.clear();
                clearContext();
            }
        };
    }

    private static void initTrace() {
        String traceId = TraceContext.initTraceId();
        MDC.put(TraceConstants.TRACE_ID, traceId);

        initThreadId();
    }

    public static <T> Callable<T> refreshWrap(final Callable<T> callable) {
        return () -> {
            initTrace();
            try {
                return callable.call();
            } finally {
                MDC.clear();
                clearContext();
            }
        };
    }

    public static Runnable refreshWrap(final Runnable runnable) {
        return () -> {
            initTrace();
            try {
                runnable.run();
            } finally {
                MDC.clear();
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
            return super.decorateTask(ThreadPoolUtil.wrap(runnable), task);
        }

        @Override
        protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
            return super.decorateTask(ThreadPoolUtil.wrap(callable), task);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
            return super.schedule(ThreadPoolUtil.wrap(task), delay, unit);
        }

        @Override
        @NonNull
        public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
            return super.schedule(ThreadPoolUtil.wrap(task), delay, unit);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
            return super.scheduleAtFixedRate(ThreadPoolUtil.wrap(task), initialDelay, period, unit);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
            return super.scheduleWithFixedDelay(ThreadPoolUtil.wrap(task), initialDelay, delay, unit);
        }

        @Override
        public void execute(Runnable task) {
            super.execute(ThreadPoolUtil.wrap(task));
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

    public static class RefreshScheduledThreadPoolExecutorMDCWrapper extends ScheduledThreadPoolExecutor {
        public RefreshScheduledThreadPoolExecutorMDCWrapper(int corePoolSize) {
            super(corePoolSize);
        }

        public RefreshScheduledThreadPoolExecutorMDCWrapper(int corePoolSize, ThreadFactory threadFactory) {
            super(corePoolSize, threadFactory);
        }

        public RefreshScheduledThreadPoolExecutorMDCWrapper(int corePoolSize, RejectedExecutionHandler handler) {
            super(corePoolSize, handler);
        }

        public RefreshScheduledThreadPoolExecutorMDCWrapper(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
            super(corePoolSize, threadFactory, handler);
        }

        @Override
        protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
            return super.decorateTask(ThreadPoolUtil.refreshWrap(runnable), task);
        }

        @Override
        protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
            return super.decorateTask(ThreadPoolUtil.refreshWrap(callable), task);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
            return super.schedule(ThreadPoolUtil.refreshWrap(task), delay, unit);
        }

        @Override
        @NonNull
        public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
            return super.schedule(ThreadPoolUtil.refreshWrap(task), delay, unit);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
            return super.scheduleAtFixedRate(ThreadPoolUtil.refreshWrap(task), initialDelay, period, unit);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
            return super.scheduleWithFixedDelay(ThreadPoolUtil.refreshWrap(task), initialDelay, delay, unit);
        }

        @Override
        public void execute(Runnable task) {
            super.execute(ThreadPoolUtil.refreshWrap(task));
        }

        @Override
        @NonNull
        public Future<?> submit(Runnable task) {
            return super.submit(ThreadPoolUtil.refreshWrap(task));
        }

        @Override
        @NonNull
        public <T> Future<T> submit(Runnable task, T result) {
            return super.submit(ThreadPoolUtil.refreshWrap(task), result);
        }

        @Override
        @NonNull
        public <T> Future<T> submit(Callable<T> task) {
            return super.submit(ThreadPoolUtil.refreshWrap(task));
        }

        @Override
        protected <T> RunnableFuture<T> newTaskFor(Runnable task, T value) {
            return super.newTaskFor(ThreadPoolUtil.refreshWrap(task), value);
        }

        @Override
        protected <T> RunnableFuture<T> newTaskFor(Callable<T> task) {
            return super.newTaskFor(ThreadPoolUtil.refreshWrap(task));
        }

        @Override
        @NonNull
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            tasks = tasks.stream().map(ThreadPoolUtil::refreshWrap).collect(Collectors.toList());

            return super.invokeAny(tasks);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            tasks = tasks.stream().map(ThreadPoolUtil::refreshWrap).collect(Collectors.toList());

            return super.invokeAny(tasks, timeout, unit);
        }

        @Override
        @NonNull
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            tasks = tasks.stream().map(ThreadPoolUtil::refreshWrap).collect(Collectors.toList());

            return super.invokeAll(tasks);
        }

        @Override
        @NonNull
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            tasks = tasks.stream().map(ThreadPoolUtil::refreshWrap).collect(Collectors.toList());

            return super.invokeAll(tasks, timeout, unit);
        }
    }

    // ========== org.springframework.scheduling.concurrent =========

    public static class ThreadPoolTaskExecutorMDCWrapper extends ThreadPoolTaskExecutor {

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
        public void execute(Runnable task) {
            super.execute(ThreadPoolUtil.wrap(task));
        }

        @Override
        public void execute(@NonNull Runnable task, long startTimeout) {
            super.execute(ThreadPoolUtil.wrap(task), startTimeout);
        }

        @Override
        @NonNull
        public Future<?> submit(Runnable task) {
            return super.submit(ThreadPoolUtil.wrap(task));
        }

        @Override
        @NonNull
        public <T> Future<T> submit(@NonNull Callable<T> task) {
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
        public ScheduledFuture<?> schedule(Runnable task, @NonNull Trigger trigger) {
            return super.schedule(ThreadPoolUtil.wrap(task), trigger);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
            return super.schedule(ThreadPoolUtil.wrap(task), startTime);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
            return super.scheduleAtFixedRate(ThreadPoolUtil.wrap(task), startTime, period);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
            return super.scheduleAtFixedRate(ThreadPoolUtil.wrap(task), period);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
            return super.scheduleWithFixedDelay(ThreadPoolUtil.wrap(task), startTime, delay);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
            return super.scheduleWithFixedDelay(ThreadPoolUtil.wrap(task), delay);
        }

        @Override
        @NonNull
        public Thread createThread(@NonNull Runnable task) {
            return super.createThread(ThreadPoolUtil.wrap(task));
        }
    }

    public static class RefreshThreadPoolTaskSchedulerMDCWrapper extends ThreadPoolTaskScheduler {

        @Override
        public void execute(Runnable task) {
            super.execute(ThreadPoolUtil.refreshWrap(task));
        }

        @Override
        public void execute(@NonNull Runnable task, long startTimeout) {
            super.execute(ThreadPoolUtil.refreshWrap(task), startTimeout);
        }

        @Override
        @NonNull
        public Future<?> submit(Runnable task) {
            return super.submit(ThreadPoolUtil.refreshWrap(task));
        }

        @Override
        @NonNull
        public <T> Future<T> submit(@NonNull Callable<T> task) {
            return super.submit(ThreadPoolUtil.refreshWrap(task));
        }

        @Override
        @NonNull
        public ListenableFuture<?> submitListenable(Runnable task) {
            return super.submitListenable(ThreadPoolUtil.refreshWrap(task));
        }

        @Override
        @NonNull
        public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
            return super.submitListenable(ThreadPoolUtil.refreshWrap(task));
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, @NonNull Trigger trigger) {
            return super.schedule(ThreadPoolUtil.refreshWrap(task), trigger);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
            return super.schedule(ThreadPoolUtil.refreshWrap(task), startTime);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
            return super.scheduleAtFixedRate(ThreadPoolUtil.refreshWrap(task), startTime, period);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
            return super.scheduleAtFixedRate(ThreadPoolUtil.refreshWrap(task), period);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
            return super.scheduleWithFixedDelay(ThreadPoolUtil.refreshWrap(task), startTime, delay);
        }

        @Override
        @NonNull
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
            return super.scheduleWithFixedDelay(ThreadPoolUtil.refreshWrap(task), delay);
        }

        @Override
        @NonNull
        public Thread createThread(@NonNull Runnable task) {
            return super.createThread(ThreadPoolUtil.refreshWrap(task));
        }
    }

    // ========== ForkJoinPool =========
    // TODO ForkJoinPool使用优化

//    public static class ForkJoinPoolMDCWrapper extends ForkJoinPool {
//        public ForkJoinPoolMDCWrapper() {
//            super();
//        }
//
//        public ForkJoinPoolMDCWrapper(int parallelism) {
//            super(parallelism);
//        }
//
//        public ForkJoinPoolMDCWrapper(int parallelism, ForkJoinWorkerThreadFactory factory, Thread.UncaughtExceptionHandler handler, boolean asyncMode) {
//            super(parallelism, factory, handler, asyncMode);
//        }
//
//        @Override
//        public <T> T invoke(ForkJoinTask<T> task) {
//            setTraceIfAbsent(true);
//            try {
//                return super.invoke(task);
//            } finally {
//                clearMDC();
//                clearContext();
//            }
//        }
//
//        @Override
//        public void execute(ForkJoinTask<?> task) {
//            setTraceIfAbsent(true);
//            try {
//                super.execute(task);
//            } finally {
//                clearMDC();
//                clearContext();
//            }
//        }
//
//        @Override
//        public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
//            setTraceIfAbsent(true);
//            try {
//                return super.submit(task);
//            } finally {
//                clearMDC();
//                clearContext();
//            }
//        }
//
//        @Override
//        public void execute(Runnable task) {
//            super.execute(ThreadPoolUtil.wrap(task));
//        }
//
//        @Override
//        @NonNull
//        public <T> ForkJoinTask<T> submit(Callable<T> task) {
//            return super.submit(ThreadPoolUtil.wrap(task));
//        }
//
//        @Override
//        @NonNull
//        public <T> ForkJoinTask<T> submit(Runnable task, T result) {
//            return super.submit(ThreadPoolUtil.wrap(task), result);
//        }
//
//        @Override
//        @NonNull
//        public ForkJoinTask<?> submit(Runnable task) {
//            return super.submit(ThreadPoolUtil.wrap(task));
//        }
//
//        @Override
//        protected <T> RunnableFuture<T> newTaskFor(Runnable task, T value) {
//            return super.newTaskFor(ThreadPoolUtil.wrap(task), value);
//        }
//
//        @Override
//        protected <T> RunnableFuture<T> newTaskFor(Callable<T> task) {
//            return super.newTaskFor(ThreadPoolUtil.wrap(task));
//        }
//
//        @Override
//        @NonNull
//        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
//            tasks = tasks.stream().map(ThreadPoolUtil::wrap).collect(Collectors.toList());
//
//            return super.invokeAny(tasks);
//        }
//
//        @Override
//        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
//            tasks = tasks.stream().map(ThreadPoolUtil::wrap).collect(Collectors.toList());
//
//            return super.invokeAny(tasks, timeout, unit);
//        }
//
//        @Override
//        @NonNull
//        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
//            tasks = tasks.stream().map(ThreadPoolUtil::wrap).collect(Collectors.toList());
//
//            return super.invokeAll(tasks, timeout, unit);
//        }
//
//        @Override
//        @NonNull
//        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
//            tasks = tasks.stream().map(ThreadPoolUtil::wrap).collect(Collectors.toList());
//
//            return super.invokeAll(tasks);
//        }
//    }
//
//    /**
//     * ForkJoinTask链路跟踪需设置jvm参数，也可在main方法类中添加如下代码
//     * <pre> {@code
//     * static {
//     *   System.setProperty("java.util.concurrent.ForkJoinPool.common.threadFactory",
//     *     ThreadPoolUtil.ForkJoinWorkerThreadFactoryMDCWrapper.class.getName());
//     * }}</pre>
//     * ForkJoinTask在lambda中parallel()被使用，也在ForkJoin执行时被调用
//     */
//    public static class ForkJoinWorkerThreadFactoryMDCWrapper implements ForkJoinPool.ForkJoinWorkerThreadFactory {
//        @Override
//        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
//            return new ForkJoinWorkerThreadMDCWrapper(pool);
//        }
//    }
//
//    public static class ForkJoinWorkerThreadMDCWrapper extends ForkJoinWorkerThread {
//        public ForkJoinWorkerThreadMDCWrapper(ForkJoinPool pool) {
//            super(pool);
//        }
//
//        @Override
//        public void run() {
//            // TODO setTraceIfAbsent
//            try {
//                super.run();
//            } finally {
//                clearMDC();
//                clearContext();
//            }
//        }
//    }

}
