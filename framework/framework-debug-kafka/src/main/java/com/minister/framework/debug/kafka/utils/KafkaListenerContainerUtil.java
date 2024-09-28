package com.minister.framework.debug.kafka.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.minister.framework.debug.kafka.enums.TopicType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.config.KafkaListenerConfigUtils;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 调试管理KafkaListenerContainer工具类
 *
 * @author QIUCHANGQING620
 * @date 2024-05-04 00:00
 */
@Slf4j
public class KafkaListenerContainerUtil {


    /**
     * 绑定topic-Endpoint信息 (服务启动时绑定关系)
     */
    public static final ConcurrentHashMap<String, MethodKafkaListenerEndpoint>
            KAFKA_LISTENER_ENDPOINT_MAP = new ConcurrentHashMap<>();

    /**
     * 绑定泳道topic-Endpoint信息 (动态绑定关系)
     */
    public static final ConcurrentHashMap<String, MethodKafkaListenerEndpoint>
            KAFKA_LISTENER_ENDPOINT_MULTI_VERSION_MAP = new ConcurrentHashMap<>();

    /**
     * 本地服务：绑定原始topic-Endpoint信息 (动态绑定关系)
     */
    public static final ConcurrentHashMap<String, MethodKafkaListenerEndpoint>
            KAFKA_LISTENER_ENDPOINT_LOCAL_ORIGINAL_MAP = new ConcurrentHashMap<>();

    /**
     * 本地服务：绑定原始远程调试topic-Endpoint信息 (动态绑定关系)
     */
    public static final ConcurrentHashMap<String, MethodKafkaListenerEndpoint>
            KAFKA_LISTENER_ENDPOINT_DEBUG_MAP = new ConcurrentHashMap<>();

    /**
     * 绑定topic-KafkaListenerContainerFactory
     */
    public static final ConcurrentHashMap<String, KafkaListenerContainerFactory>
            KAFKA_LISTENER_CONTAINER_FACTORY_MAP = new ConcurrentHashMap<>();

    public static MessageHandlerMethodFactory messageHandlerMethodFactory;

    /**
     * endpointId 前缀
     */
    private static final String ENDPOINT_ID_PREFIX = "com.express.kafka.util.MyKafkaListenerEndpointContainer#";

    /**
     * 计数器
     */
    private static final AtomicInteger COUNTER = new AtomicInteger();

    /**
     * 动态创建原始topic的监听
     *
     * @param originalTopic 原始topic
     * @param specialTopic  特殊topic（远程调试topic，泳道topic）
     * @param topicType     topic类型
     */
    public static void dynamicCreateAndStartListenerContainer(String originalTopic, String specialTopic, TopicType topicType) {
        KafkaListenerEndpointRegistry endpointRegistry = SpringUtil.getBean(KafkaListenerConfigUtils
                .KAFKA_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME, KafkaListenerEndpointRegistry.class);
        // 监听器容器容器启动状态
        boolean listenerContainerStatus;
        // Endpoint信息
        MethodKafkaListenerEndpoint endpoint;
        switch (topicType) {
            case ORIGINAL:
                // 启动原始topic监听容器
                endpoint = KAFKA_LISTENER_ENDPOINT_LOCAL_ORIGINAL_MAP.get(originalTopic);
                listenerContainerStatus = startOriginalTopicListenerContainer(endpointRegistry,
                        endpoint, topicType, originalTopic);
                if (!listenerContainerStatus) {
                    // 创建监听器容器容器并启动
                    createAndStartListenerContainer(endpointRegistry, KAFKA_LISTENER_ENDPOINT_LOCAL_ORIGINAL_MAP,
                            originalTopic, originalTopic, topicType);
                }
                break;
            case DEBUG:
                // 启动远程调试topic监听容器
                endpoint = KAFKA_LISTENER_ENDPOINT_DEBUG_MAP.get(specialTopic);
                listenerContainerStatus = startOriginalTopicListenerContainer(endpointRegistry, endpoint,
                        topicType, specialTopic);
                if (!listenerContainerStatus) {
                    createAndStartListenerContainer(endpointRegistry, KAFKA_LISTENER_ENDPOINT_DEBUG_MAP,
                            originalTopic, specialTopic, topicType);
                }
                break;
            case MULTI_VERSION:
                // 启动泳道topic监听容器
                endpoint = KAFKA_LISTENER_ENDPOINT_MULTI_VERSION_MAP.get(specialTopic);
                listenerContainerStatus = startOriginalTopicListenerContainer(endpointRegistry, endpoint,
                        topicType, specialTopic);
                if (!listenerContainerStatus) {
                    createAndStartListenerContainer(endpointRegistry, KAFKA_LISTENER_ENDPOINT_MULTI_VERSION_MAP,
                            originalTopic, specialTopic, topicType);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 尝试启动监听器容器
     *
     * @param endpointRegistry endpoint信息注册表
     * @param endpoint         endpoint信息
     * @param endpoint         endpoint信息
     * @param endpoint         endpoint信息
     * @return
     */
    private static boolean startOriginalTopicListenerContainer(KafkaListenerEndpointRegistry endpointRegistry,
                                                               MethodKafkaListenerEndpoint endpoint,
                                                               TopicType topicType, String topic) {
        if (Objects.nonNull(endpoint)) {
            // 已经创建过Endpoint
            MessageListenerContainer listenerContainer = endpointRegistry.getListenerContainer(endpoint.getId());
            if (Objects.nonNull(listenerContainer)) {
                // 已经创建过topic的消费监听器
                if (listenerContainer.isRunning()) {
                    // topic的消费监听器已启动
                    return true;
                }
                // 启动topic的消费监听器
                listenerContainer.start();
                log.info("{}的监听器容器启动成功，topic：{}", topicType.getType(), topic);
                return true;
            }
        }
        return false;
    }

    private static boolean createAndStartListenerContainer(KafkaListenerEndpointRegistry endpointRegistry,
                                                           ConcurrentHashMap<String, MethodKafkaListenerEndpoint> endpointMap,
                                                           String originalTopic, String topic, TopicType topicType) {
        // 监听器容器创建工厂
        KafkaListenerContainerFactory factory = KAFKA_LISTENER_CONTAINER_FACTORY_MAP.get(originalTopic);
        // 原始topic的MethodKafkaListenerEndpoint信息
        MethodKafkaListenerEndpoint endpoint = KAFKA_LISTENER_ENDPOINT_MAP.get(originalTopic);
        if (Objects.isNull(endpoint)) {
            return false;
        }
        // 创建需要监听的topic的Endpoint信息
        MethodKafkaListenerEndpoint topicEndpoint = new MethodKafkaListenerEndpoint();
        // 复用原始topic的MethodKafkaListenerEndpoint信息、以实现topic消息的消费共用原始topic消息的消费处理逻辑
        BeanUtils.copyProperties(endpoint, topicEndpoint);
        // id必须是唯一的
        String id = ENDPOINT_ID_PREFIX + KafkaListenerContainerUtil.COUNTER.getAndIncrement();
        topicEndpoint.setId(id);
        // 设置要监听的泳道topic
        topicEndpoint.setTopics(topic);
        // 将Endpoint信息注册到注册表
        topicEndpoint.setMessageHandlerMethodFactory(messageHandlerMethodFactory);
        // 缓存endpoint信息
        endpointMap.put(topic, topicEndpoint);
        // 创建topic的消费监听器并立即启动
        endpointRegistry.registerListenerContainer(topicEndpoint, factory, true);
        log.info("{}的监听器容器创建并启动成功，topic：{}", topicType.getType(), topic);
        return true;
    }

    /**
     * 停止对topic的消费监听
     *
     * @param topic topic名称
     */
    public static void stopMessageListenerContainer(String topic, TopicType topicType) {
        MethodKafkaListenerEndpoint endpoint = null;
        switch (topicType) {
            case ORIGINAL:
                // 本地服务原始topic
                endpoint = KAFKA_LISTENER_ENDPOINT_LOCAL_ORIGINAL_MAP.get(topic);
                break;
            case DEBUG:
                // 远程调试topic
                endpoint = KAFKA_LISTENER_ENDPOINT_DEBUG_MAP.get(topic);
                break;
            case MULTI_VERSION:
                // 泳道topic
                endpoint = KAFKA_LISTENER_ENDPOINT_MULTI_VERSION_MAP.get(topic);
                break;
            default:
                break;
        }
        if (Objects.isNull(endpoint)) {
            return;
        }
        KafkaListenerEndpointRegistry endpointRegistry = SpringUtil.getBean(KafkaListenerConfigUtils
                .KAFKA_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME, KafkaListenerEndpointRegistry.class);
        // 获取topic的消费监听器
        MessageListenerContainer listenerContainer = endpointRegistry.getListenerContainer(endpoint.getId());
        if (Objects.nonNull(listenerContainer) && listenerContainer.isRunning()) {
            // 停止监听topic
            listenerContainer.stop();
            log.info("{}的监听器容器停止监听topic：{}", topicType.getType(), topic);
        }
    }

}
