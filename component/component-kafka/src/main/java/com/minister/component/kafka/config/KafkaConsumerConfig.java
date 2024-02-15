package com.minister.component.kafka.config;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.minister.component.kafka.properties.KafkaCustomProperties;
import com.minister.component.kafka.properties.KafkaDefaultProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * kafka消费者配置
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 11:32
 */
@Configuration
@Slf4j
public class KafkaConsumerConfig implements BeanPostProcessor {

    private final Map<String, Properties> consumer;

    public KafkaConsumerConfig(KafkaDefaultProperties kafkaDefaultProperties, KafkaCustomProperties kafkaCustomProperties) {
        this.consumer = kafkaCustomProperties.getConsumer();
    }

    public static Map<String, Object> consumerConfig() {
        Map<String, Object> props = new HashMap<>(16);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaDefaultProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaDefaultProperties.getGroupId());
        // 是否自动提交偏移量，默认 true，为了避免出现重复数据和数据丢失，可以把他设为 false，然后手动提交偏移量
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, KafkaDefaultProperties.getEnableAutoCommit());
        // 自动提交时间间隔，自动提交开启时生效
//        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitInterval);
        // 该属性指定了消费者在读取一个没有偏移量的分区或者偏移量无效的情况下该如何处理
        // earliest : 当各分区下有已提交的 offset 时，从提交的 offset 开始消费；无提交 offset 时，从头开始消费分区的记录
        // latest : 当各分区下有已提交的 offset 时，从提交的 offset 开始消费；无提交的 offset 时，消费新产生的该分区下的数据（在消费者启动之后生成的记录）
        // none : 当各分区都存在已提交的 offset 时，从提交的 offset 开始消费；只要有一个分区不存在已提交的 offset ，则抛出异常
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KafkaDefaultProperties.getAutoOffsetReset());
        // 两次 poll 之间的最大间隔，默认值为5分钟。如果超过这个间隔会触发 reBalance
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, KafkaDefaultProperties.getMaxPollIntervalMs());
        // 这个参数定义了 poll 方法最多可以拉取多少条消息，默认值为100
        // 如果在拉取消息的时候新消息不足100条，那有多少返回多少；如果超过100条，每次只返回100
        // 这个默认值在有些场景下太大，有些场景很难保证能够在5min内处理完100条消息
        // 如果消费者无法在5min内处理完100条消息的话就会触发reBalance
        // 然后这批消息会被分配到另一个消费者中，还是会处理不完，这样这批消息就永远也处理不完
        // 要避免出现上述问题，提前评估好处理一条消息最长需要多少时间，然后覆盖默认的 max-poll-records 参数
        // 注：需要开启 BatchListener 批量监听才会生效，如果不开启 BatchListener 则不会出现 reBalance 情况
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, KafkaDefaultProperties.getMaxPollRecords());
        // 当 broker 多久没有收到 consumer 的心跳请求后就触发 reBalance ，默认值是10s
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, KafkaDefaultProperties.getSessionTimeout());
        // 事务隔离级别：读已提交
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, KafkaDefaultProperties.getIsolationLevel());
        // 序列化和反序列化
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put("interceptor.classes", Lists.newArrayList("com.minister.component.kafka.interceptor.KafkaTraceConsumerInterceptor"));
        return props;
    }

    public static KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory(Properties properties) {
        Integer topicConcurrency = Integer.parseInt(properties.getOrDefault("concurrency", KafkaDefaultProperties.getConcurrency()).toString());
        properties.remove("concurrency");

        Map<String, Object> consumerConfig = consumerConfig();
        for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet()) {
            consumerConfig.put(objectObjectEntry.getKey().toString(), objectObjectEntry.getValue());
        }

        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig);

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        // 在侦听器容器中运行的线程数，一般设置为 机器数 * 分区数
        factory.setConcurrency(topicConcurrency);
        // 消费监听接口监听的主题不存在时，默认会报错，所以设置为 false 忽略错误
        factory.setMissingTopicsFatal(KafkaDefaultProperties.getMissingTopicsFatal());
        // 自动提交关闭，需要设置手动消息确认
        factory.getContainerProperties().setAckMode(KafkaDefaultProperties.getAckMode());
        factory.getContainerProperties().setPollTimeout(KafkaDefaultProperties.getPollTimeout());
        // 设置为批量监听，需要用 List 接收
        factory.setBatchListener(true);
        // listener 自启动开关
        factory.setAutoStartup(KafkaDefaultProperties.getAutoStartup());
        return factory;
    }

    /**
     * 扫描所有kafka配置注入 beanName = [topic] 的所有KafkaListenerContainerFactory
     */
    private void init() {
        if (MapUtils.isEmpty(consumer)) {
            return;
        }

        for (Map.Entry<String, Properties> entry : consumer.entrySet()) {
            String topic = entry.getKey();
            KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory =
                    kafkaListenerContainerFactory(entry.getValue());
            SpringUtil.registerBean(topic, kafkaListenerContainerFactory);
            log.info("kafka [{}] listenerContainerFactory init.", topic);
        }
    }

    private boolean init = false;
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (!init) {
            init();
            init = true;
        }
        return bean;
    }

}
