package com.minister.component.mq.entity;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.rabbitmq.client.BuiltinExchangeType;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.minister.component.utils.constants.Constants.POUND_SIGN;
import static com.rabbitmq.client.BuiltinExchangeType.TOPIC;

/**
 * Mq环境配置
 * <p>使用RabbitContextBuilder构造</p>
 *
 * @author QIUCHANGQING620
 * @date 2021-6-13 12:06
 */
@Getter
public class RabbitContext {

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
    private Boolean customQueue;

    /**
     * 路由
     */
    private String routingKey;

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
    private String delayRoutingKey;

    /**
     * Map<延迟队列序号, 延迟时间(s)>(不支持热变更)
     */
    private Map<Integer, Long> delayNum2TimeMap;

    /**
     * 是否申明交换机和队列，并建立绑定关系
     */
    private boolean declareAndBind;

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
    private long waitTime;

    /**
     * 消费者日志开关
     */
    private boolean consumerLog;

    @Deprecated
    public RabbitContext(String exchange, String queue, Integer prefetchCount, Integer maxConcurrency) {
        this(exchange, queue, prefetchCount, maxConcurrency, null);
    }

    @Deprecated
    public RabbitContext(String exchange, String queue, Integer prefetchCount, Integer maxConcurrency, Set<String> ips) {
        this(exchange, queue, prefetchCount, maxConcurrency, ips, 100L);
    }

    @Deprecated
    public RabbitContext(String exchange, String queue, Integer prefetchCount, Integer maxConcurrency, Set<String> ips, long waitTime) {
        this(exchange, TOPIC, queue, false, POUND_SIGN, null, null, null, POUND_SIGN, null, true, prefetchCount, maxConcurrency, ips, waitTime, true);
    }

    @Deprecated
    public RabbitContext(String exchange, String queue, String delayExchange, String delayQueue, Long[] delayTimes, Integer prefetchCount, Integer maxConcurrency) {
        this(exchange, queue, delayExchange, delayQueue, delayTimes, prefetchCount, maxConcurrency, null);
    }

    @Deprecated
    public RabbitContext(String exchange, String queue, String delayExchange, String delayQueue, Long[] delayTimes, Integer prefetchCount, Integer maxConcurrency, Set<String> ips) {
        this(exchange, queue, delayExchange, delayQueue, delayTimes, prefetchCount, maxConcurrency, ips, 100L);
    }

    @Deprecated
    public RabbitContext(String exchange, String queue, String delayExchange, String delayQueue, Long[] delayTimes, Integer prefetchCount, Integer maxConcurrency, Set<String> ips, long waitTime) {
        this(exchange, TOPIC, queue, false, POUND_SIGN, delayExchange, TOPIC, delayQueue, POUND_SIGN, delayTimes, true, prefetchCount, maxConcurrency, ips, waitTime, true);
    }

    @Deprecated
    public RabbitContext(String exchange, String queue, String delayExchange, String delayQueue, Collection<Long> delayTimes, Integer prefetchCount, Integer maxConcurrency) {
        this(exchange, queue, delayExchange, delayQueue, delayTimes, prefetchCount, maxConcurrency, null);
    }

    @Deprecated
    public RabbitContext(String exchange, String queue, String delayExchange, String delayQueue, Collection<Long> delayTimes, Integer prefetchCount, Integer maxConcurrency, Set<String> ips) {
        this(exchange, queue, delayExchange, delayQueue, delayTimes, prefetchCount, maxConcurrency, ips, 100L);
    }

    @Deprecated
    public RabbitContext(String exchange, String queue, String delayExchange, String delayQueue, Collection<Long> delayTimes, Integer prefetchCount, Integer maxConcurrency, Set<String> ips, long waitTime) {
        this(exchange, TOPIC, queue, false, POUND_SIGN, delayExchange, TOPIC, delayQueue, POUND_SIGN, ArrayUtil.toArray(delayTimes, Long.class), true, prefetchCount, maxConcurrency, ips, waitTime, true);
    }

    public RabbitContext(String exchange, BuiltinExchangeType exchangeType, String queue, Boolean customQueue, String routingKey,
                         String delayExchange, BuiltinExchangeType delayExchangeType, String delayQueue, String delayRoutingKey,
                         Long[] delayTimes, boolean declareAndBind, Integer prefetchCount, Integer maxConcurrency,
                         Set<String> ips, long waitTime, boolean consumerLog) {
        this.exchange = exchange;
        this.exchangeType = exchangeType;
        this.queue = queue;
        this.customQueue = customQueue;
        this.routingKey = routingKey;
        this.delayExchange = delayExchange;
        this.delayExchangeType = delayExchangeType;
        this.delayQueue = delayQueue;
        this.delayRoutingKey = delayRoutingKey;
        this.declareAndBind = declareAndBind;
        this.delayNum2TimeMap = convertDelayTime(delayTimes);
        this.prefetchCount = prefetchCount;
        this.maxConcurrency = maxConcurrency;
        this.waitTime = waitTime;
        this.ips = ips;
        this.consumerLog = consumerLog;
    }

    /**
     * 获取延迟队列数量
     *
     * @return 延迟队列数量
     */
    public Integer getDelayCount() {
        return MapUtils.isEmpty(delayNum2TimeMap) ? null : this.delayNum2TimeMap.size();
    }

    /**
     * 获取延迟时间
     *
     * @param delayNum 延迟队列序号
     * @return 延迟时间
     */
    public Long getDelayTime(int delayNum) {
        return MapUtils.isEmpty(delayNum2TimeMap) ? null : this.delayNum2TimeMap.get(delayNum);
    }

    private Map<Integer, Long> convertDelayTime(Collection<Long> delayTimes) {
        if (CollectionUtil.isEmpty(delayTimes) && !CollectionUtil.hasNull(delayTimes)) {
            return null;
        }
        AtomicInteger i = new AtomicInteger(0);
        Map<Integer, Long> map = new HashMap<>(delayTimes.size());
        delayTimes.forEach(delayTime -> map.put(i.incrementAndGet(), delayTime));

        return map;
    }

    private Map<Integer, Long> convertDelayTime(Long[] delayTimes) {
        if (ArrayUtil.isEmpty(delayTimes) && !ArrayUtil.hasNull(delayTimes)) {
            return null;
        }
        AtomicInteger i = new AtomicInteger(0);
        Map<Integer, Long> map = new HashMap<>(delayTimes.length);
        for (Long delayTime : delayTimes) {
            map.put(i.incrementAndGet(), delayTime);
        }

        return map;
    }

    public void setPrefetchCount(Integer prefetchCount) {
        if (prefetchCount == null || prefetchCount <= 0) {
            throw new IllegalArgumentException("prefetchCount has to be greater than zero");
        }
        this.prefetchCount = prefetchCount;
    }

    public void setMaxConcurrency(Integer maxConcurrency) {
        if (maxConcurrency == null || maxConcurrency <= 0) {
            throw new IllegalArgumentException("maxConcurrency has to be greater than zero");
        }
        this.maxConcurrency = maxConcurrency;
    }

    public void setIps(Set<String> ips) {
        if (CollectionUtils.isNotEmpty(ips) && CollectionUtil.hasNull(ips)) {
            throw new IllegalArgumentException("when ips is not null, must has non-empty element");
        }
        this.ips = ips;
    }

    public void setWaitTime(long waitTime) {
        if (waitTime <= 0L) {
            throw new IllegalArgumentException("waitTime has to be greater than or equal zero");
        }
        this.waitTime = waitTime;
    }

    public void setConsumerLog(boolean consumerLog) {
        this.consumerLog = consumerLog;
    }

}
