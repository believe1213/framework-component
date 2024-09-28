package com.minister.framework.debug.kafka.config;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import com.minister.component.utils.constants.ApplicationConstant;
import com.minister.framework.debug.core.enums.DebugTypeEnum;
import com.minister.framework.debug.core.gray.entity.GrayTaskDispenseEntity;
import com.minister.framework.debug.core.gray.enums.GrayType;
import com.minister.framework.debug.core.gray.helper.GrayForwardHelper;
import com.minister.framework.debug.core.local.helper.DebugForwardHelper;
import com.minister.framework.debug.core.stg.cache.MultiVersionCache;
import com.minister.framework.debug.core.stg.entity.MultiVersionEntity;
import com.minister.framework.debug.core.utils.GrayDebugUtils;
import com.minister.framework.debug.kafka.enums.TopicType;
import com.minister.framework.debug.kafka.utils.KafkaListenerContainerUtil;
import com.minister.framework.debug.kafka.utils.KafkaTopicUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * 根据泳道环境信息动态停止/创建并启动泳道topic的消费监听器
 *
 * @author QIUCHANGQING620
 * @date 2024-05-04 00:00
 */
@Component
@Slf4j
public class MultiVersionKafkaScheduler {

    @Resource
    private ApplicationConstant applicationConstant;

    int initGroupConsumerFlag = 0;

    /**
     * 动态创建并启动kafka topic监听器容器
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 15000)
    public void dynamicCreateAndStartKafkaListenerContainer() {
        initGroupConsumerFlag();
        if (GrayDebugUtils.isGrayHost() && !GrayDebugUtils.isWindows()) {
            // 默认灰度服务不监听原始topic、开启topic灰度后需要判断原始topic是否有消费者，如果没有消费则需要监听原始topic
            grayServerListenerTopic();
        }
        // 当前环境为：STG或UAT环境且非本地联调环境
        DebugTypeEnum debugType = GrayDebugUtils.getDebugType(applicationConstant.getEnv());
        if (debugType == DebugTypeEnum.MULTI_VERSION && !GrayDebugUtils.isWindows()) {
            // 获取当前服务所有泳道环境信息
            List<MultiVersionEntity> multiVersionEnvList = MultiVersionCache.getMultiVersionCache();
            if (GrayDebugUtils.isGrayHost()) {
                // 泳道服务节点：动态创建topic监听器
                grayServerHandler(multiVersionEnvList);
            } else {
                // 基准服务节点：动态停止topic监听器
                baseServerHandler(multiVersionEnvList);
            }
        }
        if (GrayDebugUtils.isWindows()) {
            // 当前为本地服务
            localServerHandler();
        }
    }

    /**
     * 当前为本地服务、过滤topic调试
     */
    private void localServerHandler() {
        // 远程调试规则信息为空可能是确实为空、也可能是DebugScheduler拉取规则任务尚未开始执行、此处再拉取一次规则
        try {
            // 获取当前服务所有远程调试规则信息
            Map<String, String> debugRuleMap = DebugForwardHelper.getAllForwardCache();
            final List<String> debugTopics = new ArrayList<>();
            if (MapUtil.isNotEmpty(debugRuleMap)) {
                // 调试规则过滤，获取本机调试topic
                debugTopics.addAll(debugTopicFilter(debugRuleMap));
                debugTopics.forEach(topic -> {
                    AdminClient adminClient = KafkaTopicUtils.getAdminClientByTopic(topic);
                    if (!KafkaTopicUtils.existTopicInKafkaCluster(topic)) {
                        // 当前调试topic没有消费者、则动态创建原始topic的监听器
                        KafkaListenerContainerUtil.dynamicCreateAndStartListenerContainer(topic,
                                null, TopicType.ORIGINAL);
                        // 重新拉取一次消费组和消费者信息
                        KafkaTopicUtils.initGroupAndConsumerMap(topic);
                    }
                    String debugTopic = topic + StrPool.UNDERLINE + GrayDebugUtils.getLocalHostName();
                    // 创建远程调试topic监听器容器
                    if (Objects.nonNull(adminClient)) {
                        KafkaTopicUtils.createTopic(adminClient, debugTopic);
                    }
                    KafkaListenerContainerUtil.dynamicCreateAndStartListenerContainer(topic, debugTopic, TopicType.DEBUG);
                });
            }
            // 本地服务监听的原始topic
            Set<String> localOriginalTopics = KafkaListenerContainerUtil.KAFKA_LISTENER_ENDPOINT_LOCAL_ORIGINAL_MAP.keySet();
            localOriginalTopics.forEach(topic -> {
                if (!debugTopics.contains(topic)) {
                    // 停止监听原始topic
                    KafkaListenerContainerUtil.stopMessageListenerContainer(topic, TopicType.ORIGINAL);
                }
            });
            // 所有原始topic
            Set<String> originalTopics = KafkaListenerContainerUtil.KAFKA_LISTENER_ENDPOINT_MAP.keySet();
            originalTopics.forEach(topic -> {
                if (!debugTopics.contains(topic)) {
                    // 停止监听远程调试topic
                    String debugTopic = topic + StrPool.UNDERLINE + GrayDebugUtils.getLocalHostName();
                    KafkaListenerContainerUtil.stopMessageListenerContainer(debugTopic, TopicType.DEBUG);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 默认灰度服务不监听原始topic、开启topic灰度后需要判断原始topic是否有消费者，如果没有消费则需要监听原始topic
     */
    private void grayServerListenerTopic() {
        if (!KafkaTopicUtils.enableKafkaGray()) {
            // 关闭了kafka消息灰度
            return;
        }
        Map<String, GrayTaskDispenseEntity.RuleItem> grayRuleCache = GrayForwardHelper.getGrayRuleCache();
        if (CollectionUtils.isEmpty(grayRuleCache)) {
            return;
        }
        Set<String> originalTopics = KafkaListenerContainerUtil.KAFKA_LISTENER_ENDPOINT_MAP.keySet();
        originalTopics.forEach(topic -> {
            if (!KafkaTopicUtils.isGrayExcludeTopic(topic)) {
                String grayRuleKey = GrayType.TOPIC.getType() + "#" + topic;
                GrayTaskDispenseEntity.RuleItem grayTopicRule = grayRuleCache.get(grayRuleKey);
                if (Objects.nonNull(grayTopicRule)) {
                    // 存在topic灰度规则、判断该topic是否有消费者
                    if (!KafkaTopicUtils.existTopicInKafkaCluster(topic)) {
                        // 动态原始topic监听器
                        KafkaListenerContainerUtil.dynamicCreateAndStartListenerContainer(topic, topic, TopicType.ORIGINAL);
                        // 重新拉取一次消费组和消费者信息
                        KafkaTopicUtils.initGroupAndConsumerMap(topic);
                    }
                }
            }
        });
    }

    /**
     * 泳道服务节点：动态创建topic监听器
     *
     * @param multiVersionEnvList 泳道环境信息列表
     */
    private void grayServerHandler(List<MultiVersionEntity> multiVersionEnvList) {
        // 获取本机IP地址
        String ipAddress = GrayDebugUtils.getRegisterIp();
        Set<String> multiVersionTopicList = new HashSet<>(KafkaListenerContainerUtil
                .KAFKA_LISTENER_ENDPOINT_MULTI_VERSION_MAP.keySet());
        // 如果没有泳道规则、则停止监听泳道topic
        if (CollectionUtils.isEmpty(multiVersionEnvList)) {
            // 获取所有泳道topic
            multiVersionTopicList.forEach(multiVersionTopic ->
                    KafkaListenerContainerUtil.stopMessageListenerContainer(multiVersionTopic, TopicType.MULTI_VERSION));
        } else {
            // 存放与当前泳道服务节点IP不匹配的泳道环境配置信息
            List<MultiVersionEntity> notMatchList = new ArrayList<>(0);
            // 存放与当前泳道服务节点IP匹配的的泳道环境配置信息
            List<String> matchEnvList = new ArrayList<>(0);
            // 获取所有原始topic
            Set<String> topicList = new HashSet<>(KafkaTopicUtils.getAllTopic());
            for (MultiVersionEntity multiVersionEnv : multiVersionEnvList) {
                if (Objects.isNull(multiVersionEnv)
                        || StringUtils.isBlank(multiVersionEnv.getEnvId())
                        || StringUtils.isBlank(multiVersionEnv.getHost())) {
                    // 过滤不合法的数据、灰度节点IP与本机IP不一致的数据
                    continue;
                }
                if (!StringUtils.equals(multiVersionEnv.getHost().split("[:]")[0], ipAddress)) {
                    notMatchList.add(multiVersionEnv);
                    continue;
                }
                matchEnvList.add(multiVersionEnv.getEnvId());
                for (String topic : topicList) {
                    if (KafkaTopicUtils.ignoreTopic(topic)) {
                        // 过滤忽略的topic（有权限验证的）
                        continue;
                    }
                    AdminClient adminClient = KafkaTopicUtils.getAdminClientByTopic(topic);
                    if (Objects.nonNull(adminClient)) {
                        // 泳道topic
                        String multiVersionTopic = KafkaTopicUtils.getMultiVersionTopicName(topic, multiVersionEnv.getEnvId());
                        KafkaTopicUtils.createTopic(adminClient, multiVersionTopic);
                        // 动态创建泳道topic的消费监听器并启动监听
                        KafkaListenerContainerUtil.dynamicCreateAndStartListenerContainer(topic,
                                multiVersionTopic, TopicType.MULTI_VERSION);
                    }
                }
            }
            notMatchList.forEach(multiVersion -> {
                for (String topic : topicList) {
                    if (KafkaTopicUtils.ignoreTopic(topic)) {
                        // 过滤忽略的topic（有权限验证的）
                        continue;
                    }
                    // 泳道topic
                    String multiVersionTopic = KafkaTopicUtils.getMultiVersionTopicName(topic, multiVersion.getEnvId());
                    // 停止监听泳道topic
                    KafkaListenerContainerUtil.stopMessageListenerContainer(multiVersionTopic, TopicType.MULTI_VERSION);
                }
            });
            // 已经监听了泳道topic
            multiVersionTopicList.forEach(multiVersionTopic -> {
                // 获取泳道ID
                String envId = multiVersionTopic.substring(multiVersionTopic.lastIndexOf(StrPool.UNDERLINE) + 1);
                if (!matchEnvList.contains(envId)) {
                    // 停止监听泳道topic
                    KafkaListenerContainerUtil.stopMessageListenerContainer(multiVersionTopic, TopicType.MULTI_VERSION);
                }
            });
        }
    }

    /**
     * 基准服务节点：动态停止topic监听器
     * <p>
     * 如果当前服务有生效的泳道环境配置信息、说明已经启动了泳道服务节点、那么泳道topic的消息应该交由泳道服务节点进行消费
     * 所以基础服务节点需要动态停止监听泳道topic
     *
     * @param multiVersionEnvList 泳道环境信息列表
     */
    private void baseServerHandler(List<MultiVersionEntity> multiVersionEnvList) {
        for (MultiVersionEntity multiVersionEnv : multiVersionEnvList) {
            if (Objects.isNull(multiVersionEnv)
                    || StringUtils.isBlank(multiVersionEnv.getEnvId())
                    || StringUtils.isBlank(multiVersionEnv.getHost())) {
                // 过滤不合法的数据
                continue;
            }
            // 遍历所有原始topic，如果监听了泳道topic则停止监听（获取到了泳道环境配置信息则说明有泳道环境服务节点、泳道topic交由泳道环境服务节点监听）
            for (String topic : KafkaTopicUtils.getAllTopic()) {
                // 泳道topic
                String multiVersionTopic = KafkaTopicUtils.getMultiVersionTopicName(topic, multiVersionEnv.getEnvId());
                // 停止监听泳道topic
                KafkaListenerContainerUtil.stopMessageListenerContainer(multiVersionTopic, TopicType.MULTI_VERSION);
            }
        }
    }

    /**
     * 远程调试topic过滤
     *
     * @param debugRuleMap 调试规则
     * @return
     */
    private List<String> debugTopicFilter(Map<String, String> debugRuleMap) {
        List<String> debugTopics = new ArrayList<>();
        KafkaTopicUtils.getAllTopic().forEach(topic -> {
            String instance = debugRuleMap.get(topic);
            if (StringUtils.isNotBlank(instance)) {
                String hostName = instance.split(":", 2)[0];
                // 获取本机hostname
                String localhostName = GrayDebugUtils.getLocalHostName();
                if (Objects.equals(localhostName, hostName)) {
                    debugTopics.add(topic);
                }
            }
        });
        return debugTopics;
    }

    private void initGroupConsumerFlag() {
        if (initGroupConsumerFlag < 10) {
            Set<String> originalTopics = KafkaListenerContainerUtil.KAFKA_LISTENER_ENDPOINT_MAP.keySet();
            originalTopics.forEach(topic -> KafkaTopicUtils.initGroupAndConsumerMap(topic));
            initGroupConsumerFlag++;
        }
    }
}
