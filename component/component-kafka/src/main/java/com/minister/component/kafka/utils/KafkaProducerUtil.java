package com.minister.component.kafka.utils;

import com.google.common.collect.Maps;
import com.minister.component.kafka.exception.KafkaException;
import com.minister.component.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * kafka生产者工具类
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 13:15
 */
@Component
@Slf4j
public class KafkaProducerUtil {

    private KafkaProducerUtil() {
    }

    private static KafkaTemplate<String, String> defaultKafkaTemplate;

    private static Map<String, KafkaTemplate<String, String>> templateMap;

    public static void flush(String topic) {
        getKafkaTemplate(topic).flush();
    }

    public static void sendMessageSync(String topic, Object obj) {
        sendMessage(topic, null, null, obj, true);
    }

    public static void sendMessageAsync(String topic, Object obj) {
        sendMessage(topic, null, null, obj, false);
    }

    public static void sendMessage(String topic, Integer partition, String key, @Nullable Object obj, boolean sync) {
        try {
            KafkaTemplate<String, String> kafkaTemplate = getKafkaTemplate(topic);
            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, partition, key, JacksonUtil.bean2Json(obj));
            if (sync) {
                future.get();
            }
        } catch (Exception e) {
            log.error("kafka sendMessage error.", e);
            throw new KafkaException(e);
        }
    }

    // --------- 获取 KafkaTemplate ---------

    private static KafkaTemplate<String, String> getKafkaTemplate(String topic) {
        return templateMap.getOrDefault(topic, defaultKafkaTemplate);
    }

    // --------- 设置 KafkaTemplate ---------

    public static void refreshDefaultKafkaTemplate(KafkaTemplate<String, String> defaultKafkaTemplate) {
        if (Objects.isNull(defaultKafkaTemplate)) {
            log.error("can not set null to default KafkaTemplate");
            return;
        }
        KafkaProducerUtil.defaultKafkaTemplate = defaultKafkaTemplate;
    }

    public static void refreshCustomKafkaTemplate(Map<String, KafkaTemplate<String, String>> templateMap) {
        if (Objects.isNull(templateMap)) {
            KafkaProducerUtil.templateMap = Maps.newHashMap();
            return;
        }
        KafkaProducerUtil.templateMap = templateMap;
    }

}
