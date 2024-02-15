package com.minister.component.mq.entity;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.rabbitmq.client.BuiltinExchangeType;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static com.minister.component.utils.constants.Constants.POUND_SIGN;
import static com.rabbitmq.client.BuiltinExchangeType.*;

/**
 * RabbitMq环境变量构造器
 *
 * @author QIUCHANGQING620
 * @date 2021-06-12 11:07
 */
public class RabbitContextBuilder {

    public static RabbitContextBuilder instance() {
        return new RabbitContextBuilder();
    }

    // ----------step1----------

    public RabbitContextBuilder direct(String exchange, String queue, Integer prefetchCount, Integer maxConcurrency) {
        if (StringUtils.isBlank(exchange)) {
            throw new IllegalArgumentException("exchange can not be blank");
        }
        if (StringUtils.isBlank(queue)) {
            throw new IllegalArgumentException("queue can not be blank");
        }
        if (prefetchCount == null || prefetchCount < 0) {
            throw new IllegalArgumentException("prefetchCount neither null nor less than zero");
        }
        if (maxConcurrency == null || maxConcurrency < 0) {
            throw new IllegalArgumentException("maxConcurrency neither null nor less than zero");
        }
        build = new RabbitContextBuild(exchange, queue, DIRECT, prefetchCount, maxConcurrency);
        return this;
    }

    public RabbitContextBuilder topic(String exchange, String queue, Integer prefetchCount, Integer maxConcurrency) {
        if (StringUtils.isBlank(exchange)) {
            throw new IllegalArgumentException("exchange can not be blank");
        }
        if (StringUtils.isBlank(queue)) {
            throw new IllegalArgumentException("queue can not be blank");
        }
        if (prefetchCount == null || prefetchCount < 0) {
            throw new IllegalArgumentException("prefetchCount neither null nor less than zero");
        }
        if (maxConcurrency == null || maxConcurrency < 0) {
            throw new IllegalArgumentException("maxConcurrency neither null nor less than zero");
        }
        build = new RabbitContextBuild(exchange, queue, TOPIC, prefetchCount, maxConcurrency);
        return this;
    }

    public RabbitContextBuilder fanout(String exchange, String queue, Integer prefetchCount, Integer maxConcurrency) {
        if (StringUtils.isBlank(exchange)) {
            throw new IllegalArgumentException("exchange can not be blank");
        }
        if (StringUtils.isBlank(queue)) {
            throw new IllegalArgumentException("queue can not be blank");
        }
        if (prefetchCount == null || prefetchCount < 0) {
            throw new IllegalArgumentException("prefetchCount neither null nor less than zero");
        }
        if (maxConcurrency == null || maxConcurrency < 0) {
            throw new IllegalArgumentException("maxConcurrency neither null nor less than zero");
        }
        build = new RabbitContextBuild(exchange, queue, FANOUT, prefetchCount, maxConcurrency);
        return this;
    }

    public RabbitContextBuilder customQueue(boolean customQueue) {
        build.setCustomQueue(customQueue);
        return this;
    }

    public RabbitContextBuilder routingKey(String routingKey) {
        if (build == null) {
            throw new IllegalArgumentException("exchange and queue must be setting before this");
        }
        if (FANOUT.equals(build.getExchangeType())) {
            throw new IllegalArgumentException("can not change routingKey for FANOUT exchange");
        }
        if (StringUtils.isBlank(routingKey)) {
            throw new IllegalArgumentException("setting routingKey can not be blank");
        }
        build.setRoutingKey(routingKey);
        return this;
    }

    // ----------step2(skip)----------

    public RabbitContextBuilder delay(String delayExchange, String delayQueue, Long[] delayTimes) {
        if (build == null) {
            throw new IllegalArgumentException("exchange and queue must be setting before this");
        }
        if (StringUtils.isBlank(delayExchange)) {
            throw new IllegalArgumentException("delayExchange can not be blank");
        }
        if (StringUtils.isBlank(delayQueue)) {
            throw new IllegalArgumentException("delayQueue can not be blank");
        }
        if (ArrayUtil.isEmpty(delayTimes) || ArrayUtil.hasNull(delayTimes)) {
            throw new IllegalArgumentException("delayTimes neither empty nor has null element");
        }

        build.setDelayExchange(delayExchange);
        build.setDelayQueue(delayQueue);
        build.setDelayExchangeType(DIRECT);
        build.setDelayTimes(delayTimes);
        return this;
    }

    public RabbitContextBuilder delay(BuiltinExchangeType exchangeType, String delayExchange, String delayQueue, Long[] delayTimes) {
        if (build == null) {
            throw new IllegalArgumentException("exchange and queue must be setting before this");
        }
        if (exchangeType == null) {
            throw new IllegalArgumentException("exchangeType can not be null");
        }
        if (StringUtils.isBlank(delayExchange)) {
            throw new IllegalArgumentException("delayExchange can not be blank");
        }
        if (StringUtils.isBlank(delayQueue)) {
            throw new IllegalArgumentException("delayQueue can not be blank");
        }
        if (ArrayUtil.isEmpty(delayTimes) || ArrayUtil.hasNull(delayTimes)) {
            throw new IllegalArgumentException("delayTimes neither empty nor has null element");
        }
        build.setDelayExchange(delayExchange);
        build.setDelayQueue(delayQueue);
        build.setDelayExchangeType(exchangeType);
        build.setDelayTimes(delayTimes);
        return this;
    }

    public RabbitContextBuilder delayRoutingKey(String delayRoutingKey) {
        if (build == null) {
            throw new IllegalArgumentException("exchange and queue must be setting before this");
        }
        if (build.getDelayExchangeType() == null) {
            throw new IllegalArgumentException("delayExchange and delayQueue must be setting before this");
        }
        if (FANOUT.equals(build.getDelayExchangeType())) {
            throw new IllegalArgumentException("can not change routingKey for FANOUT delayExchange");
        }
        if (StringUtils.isBlank(delayRoutingKey)) {
            throw new IllegalArgumentException("setting delayRoutingKey can not be blank");
        }
        build.setDelayRoutingKey(delayRoutingKey);
        return this;
    }

    // ----------step3(skip)----------

    public RabbitContextBuilder declareAndBind(boolean declareAndBind) {
        if (build == null) {
            throw new IllegalArgumentException("exchange and queue must be setting before this");
        }
        build.setDeclareAndBind(declareAndBind);
        return this;
    }

    public RabbitContextBuilder ips(Set<String> ips) {
        if (build == null) {
            throw new IllegalArgumentException("exchange and queue must be setting before this");
        }
        if (CollectionUtils.isNotEmpty(ips) && CollectionUtil.hasNull(ips)) {
            throw new IllegalArgumentException("when ips is not null, must has non-empty element");
        }
        build.setIps(ips);
        return this;
    }

    public RabbitContextBuilder waitTime(long waitTime) {
        if (build == null) {
            throw new IllegalArgumentException("exchange and queue must be setting before this");
        }
        build.setWaitTime(waitTime);
        return this;
    }

    public RabbitContextBuilder consumerLog(boolean consumerLog) {
        build.setConsumerLog(consumerLog);
        return this;
    }

    // ----------step4----------

    public RabbitContext build() {
        return new RabbitContext(build.getExchange(), build.getExchangeType(), build.getQueue(), build.getCustomQueue(), build.getRoutingKey(),
                build.getDelayExchange(), build.getDelayExchangeType(), build.getDelayQueue(), build.getDelayRoutingKey(),
                build.getDelayTimes(), build.isDeclareAndBind(), build.getPrefetchCount(), build.getMaxConcurrency(),
                build.getIps(), build.getWaitTime(), build.isConsumerLog());
    }

    private RabbitContextBuild build = null;

    @Data
    public class RabbitContextBuild {

        public RabbitContextBuild(String exchange, String queue, BuiltinExchangeType exchangeType, Integer prefetchCount, Integer maxConcurrency) {
            this.exchange = exchange;
            this.queue = queue;
            this.exchangeType = exchangeType;
            this.prefetchCount = prefetchCount;
            this.maxConcurrency = maxConcurrency;
        }

        /**
         * 正常消费交换机(不支持热变更)
         */
        private String exchange;

        /**
         * 交换机类型
         */
        private BuiltinExchangeType exchangeType;

        /**
         * 正常消费队列(不支持热变更)
         */
        private String queue;

        /**
         * 是否为自定义队列名（即不增加exchange前缀）
         */
        private Boolean customQueue = false;

        /**
         * 路由
         */
        private String routingKey = POUND_SIGN;

        /**
         * 延迟交换机(不支持热变更)
         */
        private String delayExchange;

        /**
         * 延迟交换机类型
         */
        private BuiltinExchangeType delayExchangeType;

        /**
         * 延迟队列(不支持热变更)
         */
        private String delayQueue;

        /**
         * 延迟路由
         */
        private String delayRoutingKey = POUND_SIGN;

        /**
         * 是否申明交换机和队列，并建立绑定关系
         */
        private boolean declareAndBind = true;

        /**
         * 延迟时间(s)
         */
        private Long[] delayTimes;

        /**
         * 单次消费最大数量
         */
        private Integer prefetchCount;

        /**
         * 处理线程最大数
         */
        private Integer maxConcurrency;

        /**
         * 订阅者ip(不配置时，所有机器订阅，最多每隔5s判断一次订阅者ip配置)
         */
        private Set<String> ips;

        /**
         * 调用实际消费者的间隔时间(ms, 默认100ms)
         */
        private long waitTime = 100L;

        /**
         * 消费者日志开关
         */
        private boolean consumerLog = true;
    }

}
