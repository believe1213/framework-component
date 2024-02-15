package com.minister.framework.cloud.feign;

import com.minister.component.trace.constants.TraceConstants;
import com.minister.component.trace.context.TraceContext;
import com.minister.component.utils.context.HeadersContext;
import com.minister.component.utils.context.ThreadLocalContext;
import com.minister.component.utils.entity.HeaderEntity;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * Hystrix 策略
 *
 * @author QIUCHANGQING620
 * @date 2022-12-06 16:57
 */
@Component
@Slf4j
public class GlobalHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

    public GlobalHystrixConcurrencyStrategy() {
        try {
            HystrixConcurrencyStrategy delegate = HystrixPlugins.getInstance().getConcurrencyStrategy();
            if (delegate instanceof GlobalHystrixConcurrencyStrategy) {
                return;
            }
            HystrixCommandExecutionHook commandExecutionHook = HystrixPlugins.getInstance().getCommandExecutionHook();
            HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance().getEventNotifier();
            HystrixMetricsPublisher metricsPublisher = HystrixPlugins.getInstance().getMetricsPublisher();
            HystrixPropertiesStrategy propertiesStrategy = HystrixPlugins.getInstance().getPropertiesStrategy();

            HystrixPlugins.reset();
            HystrixPlugins.getInstance().registerConcurrencyStrategy(this);
            HystrixPlugins.getInstance().registerCommandExecutionHook(commandExecutionHook);
            HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
            HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
            HystrixPlugins.getInstance().registerPropertiesStrategy(propertiesStrategy);
        } catch (Exception e) {
            log.error("Fail to register Sleuth Hystrix Concurrency Strategy", e);
        }
    }

    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        final String traceId = TraceContext.getTraceId();
        final String threadId = TraceContext.getThreadId();
        final HeaderEntity headerEntity = HeadersContext.getHeaderEntity();
        return () -> {
            try {
                MDC.put(TraceConstants.TRACE_ID, traceId);
                MDC.put(TraceConstants.THREAD_ID, threadId);
                MDC.put(TraceConstants.USER_ID, headerEntity.getUserId());
                MDC.put(TraceConstants.BATCH_ID, headerEntity.getBatchId());

                return callable.call();
            } finally {
                MDC.clear();
                HeadersContext.clean();
                TraceContext.clean();
                ThreadLocalContext.clean();
            }
        };
    }

}
