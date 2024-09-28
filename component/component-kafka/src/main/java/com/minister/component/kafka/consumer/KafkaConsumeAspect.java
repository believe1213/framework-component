package com.minister.component.kafka.consumer;

import com.minister.component.trace.constants.TraceConstants;
import com.minister.component.trace.context.TraceContext;
import com.minister.component.utils.context.HeadersContext;
import com.minister.component.utils.context.ThreadLocalContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Kafka消费监听注解拦截器
 *
 * @author QIUCHANGQING620
 * @date 2024-04-23 23:05
 */
@Component
@Aspect
@Slf4j
@Order(51)
public class KafkaConsumeAspect {

    @Pointcut("@annotation(org.springframework.kafka.annotation.KafkaListener) || @annotation(org.springframework.kafka.annotation.KafkaListeners)")
    private void pointcut() {

    }

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            MDC.put(TraceConstants.THREAD_ID, TraceContext.initThreadId(false));

            String traceId = TraceContext.initTraceId();
            MDC.put(TraceConstants.TRACE_ID, traceId);

            return proceedingJoinPoint.proceed();
        } finally {
            MDC.clear();
            HeadersContext.clean();
            TraceContext.cleanAll();
            ThreadLocalContext.clean();
        }
    }

}
