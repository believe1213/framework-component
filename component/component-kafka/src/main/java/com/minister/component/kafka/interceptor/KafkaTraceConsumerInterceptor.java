package com.minister.component.kafka.interceptor;

import com.minister.component.trace.constants.TraceConstants;
import com.minister.component.trace.context.TraceContext;
import com.minister.component.utils.context.HeadersContext;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.MDC;

import java.util.Map;

/**
 * Kafka 链路跟踪消费者拦截器
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 15:35
 */
public class KafkaTraceConsumerInterceptor implements ConsumerInterceptor {

    @Override
    public ConsumerRecords onConsume(ConsumerRecords consumerRecords) {
        MDC.put(TraceConstants.THREAD_ID, TraceContext.initThreadId());
        String traceId = TraceContext.initTraceId();
        MDC.put(TraceConstants.TRACE_ID, traceId);

        // 以下逻辑仅适用于单条消费，所以此方法无效
//        Iterator<ConsumerRecord<String, String>> iterators = consumerRecords.iterator();
//        while (iterators.hasNext()) {
//            ConsumerRecord<String, String> record = iterators.next();
//            Header batchIdHeader = record.headers().lastHeader(HeadersKey.BATCH_ID);
//            if (Objects.nonNull(batchIdHeader)) {
//                String batchId = new String(batchIdHeader.value());
//                if (StringUtils.isNotBlank(batchId)) {
//                    HeaderEntity headerEntity = HeadersContext.getHeaderEntity();
//                    headerEntity.setBatchId(batchId);
//                    MDC.put(TraceConstants.BATCH_ID, batchId);
//                }
//            }
//        }

        return consumerRecords;
    }

    @Override
    public void close() {
        MDC.remove(TraceConstants.TRACE_ID);
        MDC.remove(TraceConstants.THREAD_ID);
        HeadersContext.clean();
        TraceContext.clean();
    }

    @Override
    public void onCommit(Map map) {
    }

    @Override
    public void configure(Map<String, ?> map) {
    }

}