package com.minister.component.kafka.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.google.common.collect.Maps;
import com.minister.component.kafka.properties.KafkaCustomProperties;
import com.minister.component.kafka.utils.KafkaProducerUtil;
import lombok.extern.slf4j.Slf4j;
import me.codeleep.jsondiff.common.model.JsonCompareResult;
import me.codeleep.jsondiff.common.model.JsonComparedOption;
import me.codeleep.jsondiff.core.DefaultJsonDifference;
import org.apache.commons.collections4.MapUtils;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * kafka配置项刷新
 * 以下代码有bug & kafka 包不应引入cloud包
 * org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration.EurekaClientConfigurationRefresher.onApplicationEvent
 * 需编写代码实现 refreshScope 功能
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 13:15
 */
@Slf4j
public class KafkaPropertiesChangeListener {

    private static final Lock LOCK = new ReentrantLock();

//    @Resource
//    private RefreshScope refreshScope;

    @ApolloConfigChangeListener(interestedKeys = {
            "spring.kafka.bootstrap-servers",
            "spring.kafka.producer.transaction-id-prefix",
            "spring.kafka.producer.acks",
            "spring.kafka.producer.retries",
            "spring.kafka.properties.max-request-size",
            "spring.kafka.properties.request-timeout",
            "spring.kafka.producer.batch-size",
            "spring.kafka.properties.linger-ms",
            "spring.kafka.producer.buffer-memory"
    })
    private void onChangeDefaultProducer(ConfigChangeEvent changeEvent) {
        if (!tryLock()) {
            return;
        }
        try {
            // 刷新默认生产者
            ProducerFactory<String, String> defaultProducerFactory = new DefaultKafkaProducerFactory<>(KafkaProducerConfig.producerConfig());
            KafkaTemplate<String, String> defaultKafkaTemplate = new KafkaTemplate<>(defaultProducerFactory);
            KafkaProducerUtil.refreshDefaultKafkaTemplate(defaultKafkaTemplate);
            log.info("kafka [default] producerFactory refresh.");

            // 留存原数据用于对比
            KafkaCustomProperties kafkaProperties = SpringUtil.getBean(KafkaCustomProperties.class);
            Map<String, Properties> oldProducer = kafkaProperties.getProducer();
            oldProducer = MapUtil.isEmpty(oldProducer) ? Maps.newHashMap() : oldProducer;

            // 刷新 @ConfigurationProperties
            // TODO eureka代码有bug，调用 refreshScope.refresh()会导致eureka下线
//            refreshScope.refresh(StrUtil.lowerFirst(KafkaCustomProperties.class.getSimpleName()));

            // 获取新自定义生产者配置
            kafkaProperties = SpringUtil.getBean(KafkaCustomProperties.class);
            Map<String, Properties> newProducer = kafkaProperties.getProducer();
            newProducer = MapUtil.isEmpty(newProducer) ? Maps.newHashMap() : newProducer;

            Collection<String> removeProducer = CollUtil.subtract(oldProducer.keySet(), newProducer.keySet());
            for (String topic : removeProducer) {
                log.info("kafka [{}] producerFactory remove.", topic);
            }

            Map<String, KafkaTemplate<String, String>> templateMap = Maps.newHashMap();
            if (MapUtils.isEmpty(newProducer)) {
                KafkaProducerUtil.refreshCustomKafkaTemplate(templateMap);
                return;
            }

            for (Map.Entry<String, Properties> entry : newProducer.entrySet()) {
                String topic = entry.getKey();
                Properties newProperties = entry.getValue();

                KafkaTemplate<String, String> kafkaTemplate = KafkaProducerConfig.producerFactory(newProperties);
                templateMap.put(topic, kafkaTemplate);

                if (oldProducer.containsKey(topic)) {
                    log.info("kafka [{}] producerFactory refresh.", topic);
                    continue;
                }
                log.info("kafka [{}] producerFactory init.", topic);
            }

            // 刷新 KafkaProducerUtil 中配置的生产者
            KafkaProducerUtil.refreshCustomKafkaTemplate(templateMap);
        } finally {
            LOCK.unlock();
        }
    }

    @ApolloConfigChangeListener(interestedKeyPrefixes = "kafka.producer")
    private void onChangeCustomProducer(ConfigChangeEvent changeEvent) {
        if (!tryLock()) {
            return;
        }
        try {
            // 留存原数据用于对比
            KafkaCustomProperties kafkaProperties = SpringUtil.getBean(KafkaCustomProperties.class);
            Map<String, Properties> oldProducer = kafkaProperties.getProducer();
            oldProducer = MapUtil.isEmpty(oldProducer) ? Maps.newHashMap() : oldProducer;

            // 刷新 @ConfigurationProperties
            // TODO eureka代码有bug，调用refreshScope.refresh()会导致eureka下线
//            refreshScope.refresh(StrUtil.lowerFirst(KafkaCustomProperties.class.getSimpleName()));

            // 获取新自定义生产者配置
            kafkaProperties = SpringUtil.getBean(KafkaCustomProperties.class);
            Map<String, Properties> newProducer = kafkaProperties.getProducer();
            newProducer = MapUtil.isEmpty(newProducer) ? Maps.newHashMap() : newProducer;

            Collection<String> removeProducer = CollUtil.subtract(oldProducer.keySet(), newProducer.keySet());
            for (String topic : removeProducer) {
                log.info("kafka [{}] producerFactory remove.", topic);
            }

            Map<String, KafkaTemplate<String, String>> templateMap = Maps.newHashMap();
            if (MapUtils.isEmpty(newProducer)) {
                KafkaProducerUtil.refreshCustomKafkaTemplate(templateMap);
                return;
            }

            for (Map.Entry<String, Properties> entry : newProducer.entrySet()) {
                String topic = entry.getKey();
                Properties newProperties = entry.getValue();

                KafkaTemplate<String, String> kafkaTemplate = KafkaProducerConfig.producerFactory(newProperties);
                templateMap.put(topic, kafkaTemplate);

                Properties oldProperties = oldProducer.get(topic);
                if (Objects.nonNull(oldProperties)) {
                    // 数据对比，最小刷新原则
                    JSONObject before = parseJsonObject(oldProperties);
                    JSONObject after = parseJsonObject(newProperties);
                    JsonCompareResult jsonCompareResult = new DefaultJsonDifference()
                            .option(new JsonComparedOption().setIgnoreOrder(true))
                            .detectDiff(before, after);
                    if (!jsonCompareResult.isMatch()) {
                        log.info("kafka [{}] producerFactory refresh.", topic);
                        continue;
                    }
                }
                log.info("kafka [{}] producerFactory init.", topic);
            }

            // 刷新 KafkaProducerUtil 中配置的生产者
            KafkaProducerUtil.refreshCustomKafkaTemplate(templateMap);
        } finally {
            LOCK.unlock();
        }
    }

    @ApolloConfigChangeListener(interestedKeyPrefixes = "kafka.backupProducer")
    private void onChangeCustomBackupProducer(ConfigChangeEvent changeEvent) {
        if (!tryLock()) {
            return;
        }
        try {
            // TODO refresh backup producer
        } finally {
            LOCK.unlock();
        }
    }

    private boolean tryLock() {
        boolean isLock = false;
        try {
            isLock = LOCK.tryLock(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
        return isLock;
    }

    public static JSONObject parseJsonObject(Object str) {
        try {
            String tmpStr = JSON.toJSONString(str);
            return JSON.parseObject(tmpStr);
        } catch (JSONException e) {
            log.error("parseJsonObject fail.", e);
            return null;
        }
    }

}
