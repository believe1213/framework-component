package com.minister.framework.debug.kafka.interceptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.minister.component.utils.JacksonUtil;
import com.minister.component.utils.context.HeadersContext;
import com.minister.component.utils.entity.HeaderEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;

import java.util.Map;

/**
 * 调试管理生产者拦截器
 *
 * @author QIUCHANGQING620
 * @date 2024-05-04 00:00
 */
@Slf4j
public class DebugProducerInterceptor implements ProducerInterceptor {

    @Override
    public ProducerRecord onSend(ProducerRecord producerRecord) {
        try {
            Headers producerHeaders = producerRecord.headers();

            // HeaderEntity
            HeaderEntity headerEntity = HeadersContext.getHeaderEntity();
            Map<String, String> headerMap = JacksonUtil.convertValue(headerEntity, new TypeReference<Map<String, String>>() {
            });
            setHeader(producerHeaders, headerMap);

            // customHeader
            Map<String, String> customHeader = HeadersContext.getCustomHeader();
            setHeader(producerHeaders, customHeader);

            // 灰度实例发起的消息均携带灰度头
            // if (GrayDebugUtils.isGrayHost()) {
            //     producerRecord.headers().add(X_APP_GRAY_HEADER, "1".getBytes());
            //     // 灰度节点发送的消息由灰度节点消费（仅适用自产自消场景）
            //     if (GrayDebugUtils.isWindows()){
            //         producerRecord.headers().add(Constants.TARGET_IP, GrayDebugUtils.getLocalHostName().getBytes());
            //     } else {
            //         producerRecord.headers().add(Constants.TARGET_IP, GrayDebugUtils.getRegisterIp().getBytes());
            //     }
            //     if (applicationConstant == null) {
            //         applicationConstant = SpringUtils.getBean(ApplicationConstant.class);
            //     }
            //     producerRecord.headers().add(APPLICATION_NAME, applicationConstant.getApplicationName().getBytes());
            // }
        } catch (Exception e) {
            log.warn("kafka DebugProducerInterceptor fail", e);
        }
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

    private void setHeader(Headers producerHeaders, Map<String, String> headers) {
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getValue() != null) {
                    producerHeaders.add(entry.getKey(), entry.getValue().getBytes());
                }
            }
        }
    }

}