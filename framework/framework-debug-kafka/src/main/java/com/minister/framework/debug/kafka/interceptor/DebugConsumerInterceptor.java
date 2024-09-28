package com.minister.framework.debug.kafka.interceptor;

import cn.hutool.core.text.StrPool;
import cn.hutool.extra.spring.SpringUtil;
import com.minister.component.utils.constants.ApplicationConstant;
import com.minister.component.utils.constants.HeadersKey;
import com.minister.component.utils.enums.EnvTypeEnum;
import com.minister.framework.debug.core.gray.enums.GrayType;
import com.minister.framework.debug.core.gray.helper.GrayForwardHelper;
import com.minister.framework.debug.core.local.enums.DebugType;
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
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.util.*;

/**
 * 调试管理消费者拦截器
 *
 * @author QIUCHANGQING620
 * @date 2024-05-04 00:00
 */
@Slf4j
public class DebugConsumerInterceptor implements ConsumerInterceptor {

    private GrayForwardHelper grayForwardHelper = new GrayForwardHelper();
    private DebugForwardHelper debugForwardHelper = new DebugForwardHelper();
    private ApplicationConstant applicationConstant = null;

    /**
     * 消息拦截处理
     *
     * @param consumerRecords
     * @return
     */
    @Override
    public ConsumerRecords onConsume(ConsumerRecords consumerRecords) {
        mdc();
        try {
            if (GrayDebugUtils.isGrayHost()) {
                // 灰度服务节点
                return grayServerHandler(consumerRecords);
            } else {
                // 基准服务节点
                return baseServerHandler(consumerRecords);
            }
        } catch (Exception e) {
            log.warn("kafka DebugConsumerInterceptor fail", e);
        }
        return consumerRecords;
    }

    /**
     * 灰度服务节点：处理消息
     * 1、解决个多消费组同时灰度时消费重复的问题
     * 2、将消息头中的泳道环境ID保存到Context中
     */
    private ConsumerRecords grayServerHandler(ConsumerRecords consumerRecords) {
        Map<TopicPartition, List<ConsumerRecord>> map = new HashMap<>();
        Set<TopicPartition> set = consumerRecords.partitions();
        String topic = null;
        String originalTopic;
        Set<String> currentGroups;
        for (TopicPartition topicPartition : set) {
            topic = topicPartition.topic();
            // 获取原始topic名称
            originalTopic = KafkaTopicUtils.getOriginalTopic(topic);
            // 获取消费组名称
            currentGroups = KafkaTopicUtils.getGroupsByTopic(originalTopic);
            List<ConsumerRecord> list = consumerRecords.records(topicPartition);
            List<ConsumerRecord> normalRecords = new ArrayList<>();
            for (ConsumerRecord record : list) {
                // 获取泳道环境ID
                String envId = this.getEnvIdFromConsumerRecordHeader(record);
                // 灰度节点监听原始topic
                if (KafkaTopicUtils.isOriginalTopic(topic)) {
                    String groupId = KafkaTopicUtils.getSimpleGroupByTopic(topic);
                    if (groupId != null) {
                        // 本地调试消息过滤
                        if (this.debugFilter(record, topic)) {
                            continue;
                        }
                        // 有泳道ID且非生产环境
                        if (StringUtils.isNotBlank(envId) && !EnvTypeEnum.PROD.name().equalsIgnoreCase(getApplicationConstant().getEnv())) {
                            // 如果是原始topic，且存在泳道环境ID、转发消息
                            String multiVersionTopic = KafkaTopicUtils.getMultiVersionTopicName(topic, envId);
                            log.info("泳道环境消息过滤，存在泳道环境ID：{}，当前topic：{}，转发消息到topic：{}", envId, topic, multiVersionTopic);
                            sendGrayMsg(record, multiVersionTopic, envId);
                            continue;
                        }
                        // 灰度拦截、命中灰度规则
                        if (this.grayFilter(record, topic)) {
                            // 灰度节点之所以再转发一次就是为了使后续链路继续灰度、从灰度topic消费时，需要将消息头中的参数保存到Context中，以便后续链路透传灰度标识
                            continue;
                        }
                    }
                }
                // 从消息头中获取消费组ID
                String grayGroup = getGroupIdFromMessageHeader(record);
                if (StringUtils.isNotEmpty(grayGroup) && !currentGroups.contains(grayGroup)) {
                    // 如果当前消息头中的消费组ID和当前服务消费该topic的groupId不一致，则丢失消息避免重复消费
                    continue;
                }
                normalRecords.add(record);
                // 将消息头中的泳道环境ID保存到Context中
                this.setEnvIdToContext(record);
                // 将灰度标识存入Context中
                this.setGrayFlagToContext(record);
            }
            if (normalRecords.size() > 0) {
                map.put(topicPartition, normalRecords);
            }
        }
        if (map.size() <= 0) {
            KafkaTopicUtils.commitSync(topic);
        }
        return new ConsumerRecords(map);
    }

    /**
     * 基准服务节点：处理消息
     */
    private ConsumerRecords baseServerHandler(ConsumerRecords consumerRecords) {
        Map<TopicPartition, List<ConsumerRecord>> map = new HashMap<>();
        Set<TopicPartition> partitions = consumerRecords.partitions();
        // 当前topic
        String currentTopic = null;
        // 原始topic
        String originalTopic;
        Set<String> currentGroups;
        for (TopicPartition topicPartition : partitions) {
            currentTopic = topicPartition.topic();
            originalTopic = KafkaTopicUtils.getOriginalTopic(currentTopic);
            currentGroups = KafkaTopicUtils.getGroupsByTopic(originalTopic);
            List<ConsumerRecord> consumerRecordList = consumerRecords.records(topicPartition);
            List<ConsumerRecord> normalRecords = new ArrayList<>();
            for (ConsumerRecord record : consumerRecordList) {
                String groupId = KafkaTopicUtils.getSimpleGroupByTopic(originalTopic);
                if (groupId != null) {
                    // 本地调试消息过滤
                    if (this.debugFilter(record, originalTopic)) {
                        continue;
                    }
                    // 泳道环境消息过滤
                    if (this.multiVersionFilter(record, currentTopic)) {
                        continue;
                    }
                    // 啄木鸟灰度消息过滤
                    if (this.grayFilter(record, originalTopic)) {
                        continue;
                    }
                    // 灰度节点自己发的消息过滤（容易搞混乱）
                    // if (this.sendByCurrentServerFilter(record, originalTopic)) {
                    //     continue;
                    // }
                }
                // 从消息头中获取消费组ID
                String grayGroup = getGroupIdFromMessageHeader(record);
                if (StringUtils.isNotEmpty(grayGroup) && !currentGroups.contains(grayGroup)) {
                    // 如果当前消息头中的消费组ID和当前服务消费该topic的groupId不一致，则丢失消息避免重复消费
                    continue;
                }
                normalRecords.add(record);
            }
            if (normalRecords.size() > 0) {
                map.put(topicPartition, normalRecords);
            }
        }
        if (map.size() <= 0) {
            KafkaTopicUtils.commitSync(currentTopic);
        }
        return new ConsumerRecords(map);
    }

    /**
     * 本地调试消息过滤
     *
     * @param record 消费记录
     * @param topic  原始topic
     */
    private boolean debugFilter(ConsumerRecord record, String topic) {
        if (EnvTypeEnum.PROD.name().equalsIgnoreCase(getApplicationConstant().getEnv())) {
            // 生产环境不拦截验证在线调试
            return false;
        }
        if (KafkaTopicUtils.isMultiVersionTopic(topic)) {
            // 当前topic为泳道topic不做本地调试过滤
            return false;
        }
        if (DebugForwardHelper.isCacheEmpty()) {
            // 调试规则为空
            return false;
        }
        String body = (String) record.value();
        String hostName = debugForwardHelper.getHostNameByDebugType(DebugType.TOPIC, topic, body);
        if (StringUtils.isNotEmpty(hostName)) {
            String debugTopic = topic + StrPool.UNDERLINE + hostName;
            log.info("命中调试规则topic：{}", topic);
            sendGrayMsg(record, debugTopic, null);
            return true;
        }
        return false;
    }

    /**
     * 啄木鸟灰度消息过滤
     *
     * @param record 消费记录
     * @param topic  原始topic
     * @return 是否满足规律规则
     */
    private boolean grayFilter(ConsumerRecord record, String topic) {
        if (!KafkaTopicUtils.enableKafkaGray()) {
            // 已关闭灰度
            return false;
        }
        if (KafkaTopicUtils.isGrayExcludeTopic(topic)) {
            // topic所属Kafka集群被灰度排除了
            return false;
        }
        if (KafkaTopicUtils.isMultiVersionTopic(topic)) {
            // 当前topic为泳道topic不做灰度过滤
            return false;
        }
        if (GrayForwardHelper.isCacheEmpty()) {
            return false;
        }
        String body = (String) record.value();
        // 啄木鸟灰度过滤-topic
        String ip = grayForwardHelper.getGrayIpByGayType(GrayType.TOPIC, topic, body);
        if (StringUtils.isNotEmpty(ip)) {
            String grayTopic = topic + StrPool.UNDERLINE + GrayDebugUtils.FRAMEWORK_GRAY;
            log.info("啄木鸟灰度消息过滤，命中灰度规则-topic：{}", topic);
            sendGrayMsg(record, grayTopic, null);
            return true;
        }
        // 啄木鸟灰度过滤-用户校验
        Header grayAccountHeader = record.headers().lastHeader(Constants.X_GRAY_ACCOUNT);
        String grayAccount = grayAccountHeader != null ? new String(grayAccountHeader.value()) : null;
        // 啄木鸟灰度过滤-灰度工号
        ip = grayForwardHelper.getGrayIpByGayType(GrayType.USER_NUMBER, grayAccount, body);
        if (StringUtils.isNotEmpty(ip)) {
            String grayTopic = topic + StrPool.UNDERLINE + GrayDebugUtils.FRAMEWORK_GRAY;
            log.info("啄木鸟灰度消息过滤，命中灰度规则-account：{}", grayAccount);
            sendGrayMsg(record, grayTopic, null);
            return true;
        }
        // 啄木鸟灰度过滤-灰度手机
        ip = grayForwardHelper.getGrayIpByGayType(GrayType.MOBILE, grayAccount, body);
        if (StringUtils.isNotEmpty(ip)) {
            String grayTopic = topic + StrPool.UNDERLINE + GrayDebugUtils.FRAMEWORK_GRAY;
            sendGrayMsg(record, grayTopic, null);
            return true;
        }
        // 啄木鸟灰度过滤-自定义头
        String headerValue = null;
        ip = null;
        for (Header header : record.headers()) {
            // GRAY_HEADER_PREFIX = "X-framework-"
            if (header.key().toLowerCase().startsWith(Constants.GRAY_HEADER_PREFIX.toLowerCase())) {
                headerValue = header.value() != null ? new String(header.value()) : null;
                ip = grayForwardHelper.getGrayIpByGayType(GrayType.HEADER, header.key(), headerValue);
                if (StringUtils.isNotEmpty(ip)) {
                    break;
                }
            }
        }
        if (StringUtils.isNotEmpty(ip)) {
            String hostName = GrayDebugUtils.FRAMEWORK_GRAY;
            String debugTopic = topic + StrPool.UNDERLINE + hostName;
            log.info("啄木鸟灰度消息过滤，命中灰度规则-自定义请求头：{}", headerValue);
            sendGrayMsg(record, debugTopic, null);
            return true;
        }
        return false;
    }

    /**
     * 泳道环境消息过滤
     *
     * @param record 消费记录
     * @param topic  当前topic
     * @return
     */
    private boolean multiVersionFilter(ConsumerRecord record, String topic) {
        if (EnvTypeEnum.PROD.name().equalsIgnoreCase(getApplicationConstant().getEnv())) {
            // 生产环境不做泳道环境拦截
            return false;
        }
        String body = (String) record.value();
        // 泳道环境ID
        String envId = this.getEnvIdFromConsumerRecordHeader(record);
        if (StringUtils.isBlank(envId)) {
            return false;
        }
        // 获取泳道环境配置信息
        MultiVersionEntity multiVersion = MultiVersionCache.getMultiVersion(envId);
        if (Objects.nonNull(multiVersion) && StringUtils.isNotBlank(multiVersion.getHost())) {
            // 需要转发消息
            String multiVersionTopic = topic;
            if (!KafkaTopicUtils.isMultiVersionTopic(topic)) {
                // 获取泳道topic
                multiVersionTopic = KafkaTopicUtils.getMultiVersionTopicName(topic, envId);
            }
            log.info("泳道环境消息过滤，存在泳道环境ID：{}，当前topic：{}，转发消息到topic：{}", envId, topic, multiVersionTopic);
            sendGrayMsg(record, multiVersionTopic, envId);
            return true;
        }
        // 未命中泳道环境规则、但消息头中有泳道环境ID、需要自己消费该消息
        if (KafkaTopicUtils.isMultiVersionTopic(topic)) {
            // 当前topic是泳道topic、不再需要转发消息、将环境ID存入Context
            log.info("泳道环境消息过滤，存在泳道环境ID：{}，未命中泳道环境规则由自己消费消息，当前topic是泳道topic：{}", envId, topic);
            this.setEnvIdToContext(record);
            return false;
        } else {
            // 当前topic为原始topic、需要将消息转到泳道topic
            String multiVersionTopic = KafkaTopicUtils.getMultiVersionTopicName(topic, envId);
            // 创建topic
            AdminClient adminClient = KafkaTopicUtils.getAdminClientByTopic(topic);
            if (Objects.nonNull(adminClient)) {
                KafkaTopicUtils.createTopic(adminClient, multiVersionTopic);
            }
            // 动态创建泳道topic监听器
            KafkaListenerContainerUtil.dynamicCreateAndStartListenerContainer(topic, multiVersionTopic, TopicType.MULTI_VERSION);
            log.info("泳道环境消息过滤，存在泳道环境ID：{}，未命中泳道环境规则由自己消费消息，当前topic：{}，转发消息到topic：{}",
                    envId, topic, multiVersionTopic);
            sendGrayMsg(record, multiVersionTopic, envId);
            return true;
        }
    }

    /**
     * 灰度节点自己发的消息过滤
     *
     * @param record 消费记录
     * @param topic  原始topic
     * @return
     */
    /*private boolean sendByCurrentServerFilter(ConsumerRecord record, String topic) {
        if (!KafkaTopicUtils.enableKafkaGray()) {
            return false;
        }
        if (KafkaTopicUtils.isGrayExcludeTopic(topic)) {
            return false;
        }
        Header appNameHeader = record.headers().lastHeader(Constants.APPLICATION_NAME);
        String appName = Objects.nonNull(appNameHeader) ? new String(appNameHeader.value()) : null;
        if (StringUtils.isNotEmpty(appName) &&
                appName.equalsIgnoreCase(getApplicationConstant().getApplicationName())) {
            Header targetIpHeader = record.headers().lastHeader(Constants.TARGET_IP);
            String targetIp = Objects.nonNull(targetIpHeader) ? new String(targetIpHeader.value()) : null;
            if (StringUtils.isBlank(targetIp)) {
                return false;
            }
            if (GrayDebugUtils.correctIp(targetIp)) {
                if (!GrayForwardHelper.isCacheEmpty() && KafkaTopicUtils.isOriginalTopic(topic)) {
                    // 灰度转发消息
                    String grayTopic = topic + Constants.UNDERLINE + GrayDebugUtils.FRAMEWORK_GRAY;
                    log.info("啄木鸟灰度消息过滤，由本机发出的消息由本机自己消费，当前topic：{}，转发消息到topic：{}", topic, grayTopic);
                    sendGrayMsg(record, grayTopic, null);
                    return true;
                }
            }
        }
        return false;
    }*/

    /**
     * 获取消息头中的groupId
     *
     * @param record 消息
     * @return
     */
    private String getGroupIdFromMessageHeader(ConsumerRecord record) {
        Header groupHeader = record.headers().lastHeader(GrayDebugUtils.GROUP_NAME);
        String groupId = Objects.nonNull(groupHeader) ? new String(groupHeader.value()) : null;
        return removeQuotationMark(groupId);
    }

    /**
     * 从消息头信息中获取泳道环境ID
     *
     * @param record 消费记录
     * @return 泳道环境ID
     */
    private String getEnvIdFromConsumerRecordHeader(ConsumerRecord record) {
        Header envIdHeader = record.headers().lastHeader(HeadersKey.ENV_ID);
        String envId = Objects.nonNull(envIdHeader) ? new String(envIdHeader.value()) : null;
        return removeQuotationMark(envId);
    }

    private ApplicationConstant getApplicationConstant() {
        if (Objects.isNull(applicationConstant)) {
            applicationConstant = SpringUtil.getBean(ApplicationConstant.class);
        }
        return applicationConstant;
    }

    @Override
    public void onCommit(Map map) {
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }

    /**
     * 转发消息
     *
     * @param record 原消息对象
     * @param topic  目标topic
     * @param envId  泳道环境Id
     */
    private void sendGrayMsg(ConsumerRecord record, String topic, String envId) {
        String originalTopic = record.topic();
        KafkaTemplate kafkaTemplate = KafkaTopicUtils.getKafkaTemplateByTopic(originalTopic);
        if (Objects.isNull(kafkaTemplate)) {
            return;
        }
        String groupId = KafkaTopicUtils.getSimpleGroupByTopic(originalTopic);
        MessageBuilder<String> messageBuilder = MessageBuilder.withPayload((String) record.value())
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.MESSAGE_KEY, record.key())
                .setHeader(GrayDebugUtils.GROUP_NAME, groupId)
                .setHeader(HeadersKey.ENV_ID, envId);
        // 转发的消息都加上灰度标识
        // GRAY_MESSAGE_HEADER = "X-framework-kafkaGray"
        messageBuilder.setHeader(Constants.GRAY_MESSAGE_HEADER, "1");
        Headers headers = record.headers();
        for (Header header : headers) {
            String key = header.key();
            // GRAY_HEADER_PREFIX = "X-framework-"
            if (key.toLowerCase().startsWith(Constants.GRAY_HEADER_PREFIX.toLowerCase())) {
                messageBuilder.setHeader(key, removeQuotationMark(new String(header.value())));
            }
        }
        // 还需要透传其他消息头
        kafkaTemplate.send(messageBuilder.build());
        log.info("kafka消息转发，groupId={}, topic={}, envId={}", groupId, topic, envId);
    }

    /**
     * 将消息头中的泳道环境ID保存到Context中
     *
     * @param record 消费记录
     */
    private void setEnvIdToContext(ConsumerRecord record) {
        String envId = this.getEnvIdFromConsumerRecordHeader(record);
        if (StringUtils.isNotBlank(envId)) {
            Map<String, String> headers = this.getRequestHeader();
            headers.put(HeadersKey.ENV_ID, envId);
            log.info("将消息头中envId：{}，存入Context中", envId);
        }
    }

    /**
     * 将消息头中的泳道环境ID保存到Context中
     *
     * @param record 消费记录
     */
    private void setGrayFlagToContext(ConsumerRecord record) {
        Map<String, String> requestHeader = getRequestHeader();
        // 设置固定请求头，灰度消息标识、如果后续链路需要继续灰度则配置该自定义灰度头
        // GRAY_MESSAGE_HEADER = "X-framework-kafkaGray"
        requestHeader.put(Constants.GRAY_MESSAGE_HEADER, "1");
        Headers headers = record.headers();
        for (Header header : headers) {
            String key = header.key();
            // 将X-framework-开头的请求头参数设置到Context中
            // GRAY_HEADER_PREFIX = "X-framework-"
            if (key.toLowerCase().startsWith(Constants.GRAY_HEADER_PREFIX.toLowerCase())) {
                requestHeader.put(key, removeQuotationMark(new String(header.value())));
            }
        }
    }

    private void mdc() {
        String traceId = UUID.randomUUID().toString().replaceAll("-", "");
        Context context = ContextUtils.get();
        // 如果Context中已有traceId，则直接使用Context中的traceId
        if (Objects.nonNull(context) && StringUtils.isNotBlank(context.getTraceId())) {
            traceId = context.getTraceId();
        }
        MDC.put("SOFA-TraceId", traceId);
    }

    /**
     * 获取Context
     *
     * @return
     */
    private Context getContext() {
        Context context = ContextUtils.get();
        if (Objects.isNull(context)) {
            context = new Context();
            ContextUtils.set(context);
        }
        return context;
    }

    /**
     * 获取Context 中的请求头信息
     *
     * @return
     */
    private Map<String, String> getRequestHeader() {
        Context context = getContext();
        Map<String, String> headers = (Map<String, String>) context.getGlobalVariable(Constants.REQUEST_HEADER);
        if (Objects.isNull(headers)) {
            headers = new HashMap<>();
            context.addGlobalVariable(Constants.REQUEST_HEADER, headers);
        }
        return headers;
    }

    /**
     * 去除引号
     *
     * @param value
     * @return
     */
    private String removeQuotationMark(String value) {
        if (StringUtils.isNotBlank(value)) {
            return value.replaceAll("\"", "");
        }
        return null;
    }

}