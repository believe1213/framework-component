package com.minister.component.kafka.consumer;

import com.minister.component.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Kafka消费者抽象类
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 15:17
 */
@Slf4j
public abstract class AbstractKafkaConsumer<T> {

    protected abstract void onReceiveMessage(List<String> records, Acknowledgment ack);

    @SuppressWarnings("unchecked")
    protected void processReceiveMessage(List<String> records, Acknowledgment ack, Class<T> clazz) {
        try {
            if (CollectionUtils.isEmpty(records)) {
                return;
            }

            List<T> messageList;
            if (String.class.equals(clazz)) {
                messageList = (List<T>) records.stream()
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList());
            } else {
                messageList = records.stream()
                        .map(s -> JacksonUtil.json2Bean(s, clazz))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }

            boolean status = consumeMessage(messageList);
            if (status) {
                ack.acknowledge();
            } else {
                ack.nack(0, 3000L);
            }
        } catch (Exception ex) {
            log.error("kafka consumer error.", ex);
            ack.nack(0, 60000L);
        }
    }

    /**
     * 实际消费方法
     * 返回false：3s后重试
     * 抛出RuntimeException：60s后重试
     */
    protected abstract boolean consumeMessage(List<T> messageList);

}
