package com.minister.component.kafka.config;

import com.google.common.collect.Maps;
import com.minister.component.kafka.properties.KafkaCustomProperties;
import com.minister.component.kafka.properties.KafkaDefaultProperties;
import com.minister.component.kafka.utils.KafkaProducerUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * kafka生产者配置
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 13:15
 */
@Configuration
@Slf4j
public class KafkaProducerConfig implements InitializingBean {

    private final Map<String, Properties> producer;

    public KafkaProducerConfig(KafkaDefaultProperties kafkaDefaultProperties, KafkaCustomProperties kafkaCustomProperties) {
        this.producer = kafkaCustomProperties.getProducer();
    }

    public static Map<String, Object> producerConfig() {
        Map<String, Object> props = new HashMap<>(16);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaDefaultProperties.getBootstrapServers());
        // acks=0 : 生产者在成功写入消息之前不会等待任何来自服务器的响应
        // acks=1 : 只要集群的leader节点收到消息，生产者就会收到一个来自服务器的成功响应
        // acks=all : 只有当所有参与复制的节点全部收到消息时，生产者才会收到一个来自服务器的成功响应
        // 开启事务必须设为all
        props.put(ProducerConfig.ACKS_CONFIG, KafkaDefaultProperties.getAcks());
        // 发生错误后，消息重发的次数，开启事务必须大于0
        props.put(ProducerConfig.RETRIES_CONFIG, KafkaDefaultProperties.getRetries());
//        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, KafkaDefaultProperties.getMaxRequestSize());
        // 设置 broker 响应时间，如果 broker 在60秒之内还是没有返回给 producer 确认消息，则认为发送失败
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, KafkaDefaultProperties.getRequestTimeout());
        // 调用 send() 方法后响应时间，值必须大于 requestTimeout + lingerMs
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, KafkaDefaultProperties.getDeliveryTimeout());
//        props.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, KafkaDefaultProperties.getTransactionTimeout());
        // 当多个消息发送到相同分区时，生产者会将消息打包到一起，以减少请求交互，而不是一条条发送
        // 批次的大小可以通过 batch-size 参数设置，默认是 16KB
        // 较小的批次大小有可能降低吞吐量（批次大小为0则完全禁用批处理）
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, KafkaDefaultProperties.getBatchSize());
        // 有的时刻消息比较少，很久都凑不到指定批次的大小，会导致消息延迟很大，需设置一个时间保证批次发送
        props.put(ProducerConfig.LINGER_MS_CONFIG, KafkaDefaultProperties.getLingerMs());
        // 生产者内存缓冲区大小
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, KafkaDefaultProperties.getBufferMemory());
        // 序列化和反序列化
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        props.put("interceptor.classes", Lists.newArrayList("com.minister.component.kafka.interceptor.KafkaTraceProducerInterceptor"));
        return props;
    }

    public static KafkaTemplate<String, String> producerFactory(Properties properties) {
        Map<String, Object> consumerConfig = producerConfig();
        for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet()) {
            consumerConfig.put(objectObjectEntry.getKey().toString(), objectObjectEntry.getValue());
        }

        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(consumerConfig);

        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * 扫描所有kafka配置注入 beanName = [topic]PRODUCER_BEAN_SUFFIX 的所有KafkaTemplate
     */
    private void init() {
        // 初始化默认生产者
        ProducerFactory<String, String> defaultProducerFactory = new DefaultKafkaProducerFactory<>(producerConfig());
        KafkaTemplate<String, String> defaultKafkaTemplate = new KafkaTemplate<>(defaultProducerFactory);
        KafkaProducerUtil.refreshDefaultKafkaTemplate(defaultKafkaTemplate);
        log.info("kafka [default] producerFactory init.");

        // 初始化自定义生产者
        Map<String, KafkaTemplate<String, String>> templateMap = Maps.newHashMap();
        if (MapUtils.isEmpty(producer)) {
            KafkaProducerUtil.refreshCustomKafkaTemplate(templateMap);
        } else {
            for (Map.Entry<String, Properties> entry : producer.entrySet()) {
                String topic = entry.getKey();
                KafkaTemplate<String, String> kafkaTemplate = producerFactory(entry.getValue());
                templateMap.put(topic, kafkaTemplate);
                log.info("kafka [{}] producerFactory init.", topic);
            }
            KafkaProducerUtil.refreshCustomKafkaTemplate(templateMap);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

}
