package com.minister.framework.debug.kafka.utils;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.text.StrPool;
import cn.hutool.extra.spring.SpringUtil;
import com.minister.framework.debug.core.utils.GrayDebugUtils;
import com.minister.framework.debug.kafka.prop.KafkaSwitchConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 调试管理topic工具类
 *
 * @author QIUCHANGQING620
 * @date 2024-05-04 00:00
 */
@Slf4j
public class KafkaTopicUtils {


    /**
     * 哈希映射关系： MAP<Topic:BootstrapHost>
     */
    private static Map<String, String> bootstrapMap = new HashMap<>();

    /**
     * 哈希映射关系： MAP<BootstrapHost:AdminClient>
     */
    private static Map<String, AdminClient> adminClientMap = new HashMap<>();

    /**
     * 哈希映射关系： MAP<BootstrapHost:KafkaTemplate>
     */
    private static Map<String, KafkaTemplate<Integer, String>> kafkaTemplateMap = new HashMap<>();

    /**
     * topic所在的groupId <topic,List<group>>
     */
    private static Map<String, Set<String>> topicGroupsMap = new HashMap<>();

    private static Map<String, List<KafkaConsumer>> topicKafkaConsumer = new HashMap<>();

    /**
     * group和 offset的映射关系
     */
    private static Map<String, Map<TopicPartition, OffsetAndMetadata>> groupAndOffsetAndMetadataMap = new HashMap<>();

    /**
     * group和 consumer的映射关系
     */
    private static Map<String, ConsumerGroupDescription> groupAndConsumerMap = new HashMap<>();

    /**
     * 忽略的topic
     */
    private static Set<String> ignoreTopics = new HashSet<>();

    /**
     * 缓存动态创建的topic
     */
    private static Set<String> createTopicSet = new ConcurrentHashSet<>();

    /**
     * 定义泳道环境topic标识
     */
    public static final String MULTI_VERSION_TOPIC_FLAG = "_swimEnv_";

    /**
     * 灰度topic后缀
     */
    public static final String GRAY_TOPIC_SUFFIX = "_" + GrayDebugUtils.FRAMEWORK_GRAY;

    private static final String SASL_MECHANISM = "DATAHUB";

    private static final String SECURITY_PROTOCOL = "SASL_PLAINTEXT";

    private static final String SASL_CLIENT_CALLBACK_HANDLER_CLASS = "com.kyexpress.datahub.client.customize.DataHubSaslClientCallbackHandler";

    /**
     * 判断kafka集群是否开启鉴权
     */
    private static boolean isOpenAuthKafkaCluster(Properties adminProps) {
        String jaasConfig = adminProps.getProperty(SaslConfigs.SASL_JAAS_CONFIG);
        if (StringUtils.isNotBlank(jaasConfig)) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是大数据新版本kafka集群
     */
    private static boolean isNewAuthBigDataKafkaCluster(Properties adminProps) {
        String saslMechanism = adminProps.getProperty(SaslConfigs.SASL_MECHANISM);
        if (Objects.equals(SASL_MECHANISM, saslMechanism)) {
            return true;
        }
        return false;
    }

    /**
     * 初始化adminClient与topic关系
     */
    public static void initAdminClients(String topic, String bootstrapHost, Properties adminProps) {
        try {
            // 如果开启了鉴权，但不是大数据kafka新版本鉴权机制则忽略该topic
            if (isOpenAuthKafkaCluster(adminProps) && !isNewAuthBigDataKafkaCluster(adminProps)) {
                // 判断如果需要授权则不处理有账号密码授权的kafka集群，因为很多账号受限，无法创建topic
                ignoreTopics.add(topic);
                return;
            }
            // 登记topic和groupId关联关系
            String groupId = adminProps.getProperty(CommonClientConfigs.GROUP_ID_CONFIG);
            if (StringUtils.isNotEmpty(groupId)) {
                groupId = groupId.replaceAll("\"", "");
                Set<String> groups = topicGroupsMap.get(topic);
                if (groups == null) {
                    groups = new HashSet<>();
                }
                groups.add(groupId);
                topicGroupsMap.put(topic, groups);
            }
            // 登记topic与bootstrapHost关联关系
            bootstrapMap.put(topic, bootstrapHost);
            // 登记bootstrapHost与adminClient关联关系
            if (adminClientMap.get(bootstrapHost) == null) {
                AdminClient adminClient = KafkaAdminClient.create(adminProps);
                adminClientMap.put(bootstrapHost, adminClient);
            }
            // 登记bootstrapHost与KafkaTemplate关联关系
            if (kafkaTemplateMap.get(bootstrapHost) == null) {
                Map<String, Object> producerProps = new HashMap<>();
                producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapHost);
                producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                if (isNewAuthBigDataKafkaCluster(adminProps)) {
                    producerProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL);
                    producerProps.put(SaslConfigs.SASL_MECHANISM, SASL_MECHANISM);
                    producerProps.put(SaslConfigs.SASL_JAAS_CONFIG, adminProps.getProperty(SaslConfigs.SASL_JAAS_CONFIG));
                    producerProps.put(SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS, SASL_CLIENT_CALLBACK_HANDLER_CLASS);
                }
                ProducerFactory<Integer, String> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
                kafkaTemplateMap.put(bootstrapHost, new KafkaTemplate<>(producerFactory));
            }
        } catch (Exception e) {
            log.error("初始化adminClient失败", e);
        }
    }

    /**
     * 根据topic名称获取AdminClient
     *
     * @param topic
     * @return
     */
    public static AdminClient getAdminClientByTopic(String topic) {
        String bootstrapHost = bootstrapMap.get(topic);
        if (StringUtils.isNotEmpty(bootstrapHost)) {
            return adminClientMap.get(bootstrapHost);
        }
        return null;
    }

    /**
     * 根据topic名称获取KafkaTemplate
     *
     * @param topic
     * @return
     */
    public static KafkaTemplate<Integer, String> getKafkaTemplateByTopic(String topic) {
        String bootstrapHost = bootstrapMap.get(topic);
        if (StringUtils.isNotEmpty(bootstrapHost)) {
            return kafkaTemplateMap.get(bootstrapHost);
        }
        return null;
    }

    /**
     * 判断topic是否存在kafka集群里有消费者，true标识存在
     *
     * @param topic
     */
    public static boolean existTopicInKafkaCluster(String topic) {
        Set<String> groups = topicGroupsMap.get(topic);
        if (CollectionUtils.isEmpty(groups)) {
            return false;
        }
        String groupId = getFirstGroup(groups);
        String bootstrap = bootstrapMap.get(topic);
        String gmKey = bootstrap + groupId;
        ConsumerGroupDescription description = groupAndConsumerMap.get(gmKey);
        Collection<MemberDescription> collection = description.members();
        if (CollectionUtils.isEmpty(collection)) {
            return false;
        }
        for (MemberDescription md : collection) {
            Set<TopicPartition> partitions = md.assignment().topicPartitions();
            for (TopicPartition tp : partitions) {
                if (tp.topic().equalsIgnoreCase(topic)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 拉取消费组和消费者信息
     *
     * @param topic
     */
    public static void initGroupAndConsumerMap(String topic) {
        try {
            Set<String> groups = topicGroupsMap.get(topic);
            if (CollectionUtils.isEmpty(groups)) {
                return;
            }
            String groupId = getFirstGroup(groups);
            String bootstrap = bootstrapMap.get(topic);
            AdminClient adminClient = adminClientMap.get(bootstrap);
            DescribeConsumerGroupsResult dg = adminClient.describeConsumerGroups(Arrays.asList(groupId));
            KafkaFuture<ConsumerGroupDescription> kafkaFuture = dg.describedGroups().get(groupId);
            ConsumerGroupDescription groupDescription = kafkaFuture.get();
            String gmKey = bootstrap + groupId;
            groupAndConsumerMap.put(gmKey, groupDescription);
        } catch (Exception e) {
            log.error("获取Consumer Group Description失败", e);
        }
    }

    /**
     * 忽略的topic
     */
    public static boolean ignoreTopic(String topic) {
        return ignoreTopics.contains(topic);
    }

    private static String getFirstGroup(Set<String> groups) {
        for (String g : groups) {
            return g;
        }
        return null;
    }

    private static long getOffset(String topic) {
        Collection<Map<TopicPartition, OffsetAndMetadata>> collection = groupAndOffsetAndMetadataMap.values();
        if (CollectionUtils.isEmpty(collection)) {
            return 0;
        }
        for (Map<TopicPartition, OffsetAndMetadata> map : collection) {
            OffsetAndMetadata metadata = map.get(new TopicPartition(topic, 0));
            if (metadata != null && metadata.offset() > 0) {
                return metadata.offset();
            }
            metadata = map.get(new TopicPartition(topic, 1));
            if (metadata != null && metadata.offset() > 0) {
                return metadata.offset();
            }
            metadata = map.get(new TopicPartition(topic, 2));
            if (metadata != null && metadata.offset() > 0) {
                return metadata.offset();
            }
        }
        return 0;
    }

    /**
     * 创建 灰度topic 1分区，1副本
     *
     * @param topic
     * @return
     */
    public static boolean createTopic(AdminClient adminClient, String topic) {
        if (createTopicSet.contains(topic)) {
            return true;
        }
        NewTopic newTopic = new NewTopic(topic, 1, (short) 2);
        Collection<NewTopic> newTopicList = new ArrayList<>();
        newTopicList.add(newTopic);
        CreateTopicsResult result = adminClient.createTopics(newTopicList);
        createTopicSet.add(topic);
        try {
            result.all().get();
        } catch (Exception e) {
            log.debug("创建topic失败！，{}", e.getMessage());
        }
        return true;
    }

    /**
     * 创建 灰度topic 1分区，1副本
     *
     * @param topic
     * @return
     */
    public static boolean deleteTopic(AdminClient adminClient, String topic) {
        if (StringUtils.isNotBlank(topic)) {
            Collection<String> deleteTopics = new ArrayList<>();
            deleteTopics.add(topic);
            adminClient.deleteTopics(deleteTopics);
        }
        return true;
    }

    public static Set<String> getGroupsByTopic(String topic) {
        return topicGroupsMap.get(topic);
    }

    public static String getSimpleGroupByTopic(String topic) {
        if (Objects.isNull(topicGroupsMap.get(topic))) {
            return null;
        }
        if (topicGroupsMap.get(topic).size() != 1) {
            return null;
        }
        String group = topicGroupsMap.get(topic).stream().findFirst().get();
        return group;
    }

    public static void putTopicConsumer(String realTopic, KafkaConsumer kafkaConsumer) {
        List<KafkaConsumer> list = topicKafkaConsumer.get(realTopic);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(kafkaConsumer);
        topicKafkaConsumer.put(realTopic, list);
    }

    public static List<KafkaConsumer> getConsumers(String topic) {
        return topicKafkaConsumer.get(topic);
    }

    /**
     * 异步提交
     *
     * @param topic
     */
    public static void commitAsync(String topic) {
        long threadId = Thread.currentThread().getId();
        for (KafkaConsumer consumer : topicKafkaConsumer.get(topic)) {
            if (threadId == consumer.getCurrentThread()) {
                consumer.commitAsync();
                break;
            }
        }
        return;
    }

    /**
     * 同步提交
     *
     * @param topic
     */
    public static void commitSync(String topic) {
        long threadId = Thread.currentThread().getId();
        for (KafkaConsumer consumer : topicKafkaConsumer.get(topic)) {
            if (threadId == consumer.getCurrentThread()) {
                consumer.commitSync();
                log.debug(">>>>>>>>>>>>>>>>>>topic提交offset=" + topic);
                break;
            }
        }
        return;
    }

    /**
     * 获取kafka所有原始topic
     *
     * @return
     */
    public static List<String> getAllTopic() {
        return new ArrayList<>(KafkaListenerContainerUtil.KAFKA_LISTENER_ENDPOINT_MAP.keySet());
    }

    /**
     * 获取泳道topic名称
     *
     * @param topic 原始topic名称
     * @param envId 泳道环境ID
     * @return 泳道topic名称
     */
    public static String getMultiVersionTopicName(String topic, String envId) {
        return topic + MULTI_VERSION_TOPIC_FLAG + envId;
    }

    /**
     * 获取灰度topic名称
     *
     * @param topic 原始topic名称
     * @return 灰度topic名称
     */
    public static String getGrayTopicName(String topic) {
        return topic + GRAY_TOPIC_SUFFIX;
    }

    /**
     * 获取原始topic
     *
     * @param topic 当前topic
     * @return 原始topic
     */
    public static String getOriginalTopic(String topic) {
        if (StringUtils.isBlank(topic)) {
            return null;
        }
        // 灰度或在线调试topic
        if (topic.contains(GrayDebugUtils.getGrayDebugTopicAndQueueSuffix())) {
            String replace = StrPool.UNDERLINE + GrayDebugUtils.getGrayDebugTopicAndQueueSuffix();
            return topic.replaceAll(replace, "");
        }
        // 老版本的多版本topic后缀名：ip后缀
        if (topic.contains(GrayDebugUtils.getRegisterIp())) {
            String replace = StrPool.UNDERLINE + GrayDebugUtils.getRegisterIp();
            return topic.replaceAll(replace, "");
        }
        // 泳道topic
        if (topic.contains(KafkaTopicUtils.MULTI_VERSION_TOPIC_FLAG)) {
            return topic.substring(0, topic.indexOf(KafkaTopicUtils.MULTI_VERSION_TOPIC_FLAG));
        }
        // 不存在泳道topic标识、即原始topic
        return topic;
    }

    /**
     * 判断topic是否为泳道topic
     *
     * @param topic 当前topic
     * @return 是否为泳道topic
     */
    public static boolean isMultiVersionTopic(String topic) {
        if (StringUtils.isBlank(topic)) {
            return false;
        }
        return topic.indexOf(KafkaTopicUtils.MULTI_VERSION_TOPIC_FLAG) >= NumberUtils.INTEGER_ZERO;
    }

    /**
     * 判断topic是否为泳道topic
     *
     * @param topic 当前topic
     * @return 是否为泳道topic
     */
    public static boolean isOriginalTopic(String topic) {
        if (StringUtils.isBlank(topic)) {
            return false;
        }
        if (isMultiVersionTopic(topic)) {
            return false;
        }
        if (topic.endsWith(GrayDebugUtils.FRAMEWORK_GRAY)) {
            return false;
        }
        if (topic.endsWith(GrayDebugUtils.getLocalHostName())) {
            return false;
        }
        if (topic.indexOf(StrPool.UNDERLINE) > 0) {
            String ip = topic.substring(topic.lastIndexOf(StrPool.UNDERLINE) + 1);
            String regex = "^((2((5[0-5])|([0-4]\\d)))|([0-1]?\\d{1,2}))(\\.((2((5[0-5])|([0-4]\\d)))|([0-1]?\\d{1,2}))){3}$";
            if (ip.matches(regex)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 灰度节点消费kafka消息开关
     * 关闭后，灰度节点不会监听消费kafka消息，无法使用kafka消息灰度功能
     *
     * @return
     */
    public static boolean enableKafkaGray() {
        KafkaSwitchConfig kafkaSwitchConfig = SpringUtil.getApplicationContext().getBean(KafkaSwitchConfig.class);
        return kafkaSwitchConfig.isEnableKafkaGray();
    }

    /**
     * 判断当前kafka集群是否被灰度排除
     *
     * @param bootstrapServers kafka集群节点
     * @return 当前kafka集群是否被灰度排除
     */
    public static boolean isGrayExcludeKafkaServer(List<String> bootstrapServers) {
        KafkaSwitchConfig kafkaSwitchConfig = SpringUtil.getApplicationContext().getBean(KafkaSwitchConfig.class);
        String grayExcludeServer = kafkaSwitchConfig.getGrayExcludeServer();
        if (StringUtils.isBlank(grayExcludeServer)) {
            return false;
        }
        Set<String> grayExcludeServers = new HashSet<>();
        String[] kafkaServerArray = grayExcludeServer.split(";");
        for (String kafkaServerItem : kafkaServerArray) {
            if (StringUtils.isNotBlank(kafkaServerItem)) {
                grayExcludeServers.addAll(Arrays.stream(kafkaServerItem.split(",")).collect(Collectors.toSet()));
            }
        }
        for (String brokerItem : bootstrapServers) {
            if (grayExcludeServers.contains(brokerItem)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断当前topic是否是灰度排除kafka集群的topic
     *
     * @param topic topic名称
     * @return topic是否被灰度排除
     */
    public static boolean isGrayExcludeTopic(String topic) {
        String bootstrapHost = bootstrapMap.get(topic);
        if (StringUtils.isBlank(bootstrapHost)) {
            return false;
        }
        List<String> bootstrapServers = Arrays.asList(bootstrapHost.split(","));
        return isGrayExcludeKafkaServer(bootstrapServers);
    }

}
