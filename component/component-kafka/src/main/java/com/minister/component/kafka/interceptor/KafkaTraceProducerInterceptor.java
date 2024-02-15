package com.minister.component.kafka.interceptor;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

/**
 * Kafka 链路跟踪生产者拦截器
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 15:32
 */
public class KafkaTraceProducerInterceptor implements ProducerInterceptor {

    @Override
    public ProducerRecord onSend(ProducerRecord producerRecord) {
        // 以下逻辑仅适用于单条消费，所以此方法无效
//        HeaderEntity headerEntity = HeadersContext.getHeaderEntity();
//        String batchId = headerEntity.getBatchId();
//        if (StringUtils.isNotBlank(batchId)) {
//            producerRecord.headers().add(HeadersKey.BATCH_ID, batchId.getBytes());
//        }

        return producerRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> map) {
    }

}

