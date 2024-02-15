package com.minister.component.mq;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minister.component.mq.entity.RabbitContext;
import com.minister.component.mq.entity.RabbitMqMessage;
import com.minister.component.trace.utils.ThreadPoolUtil;
import com.minister.component.utils.IpUtil;
import com.minister.component.utils.JacksonUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static cn.hutool.core.text.CharPool.DASHED;
import static cn.hutool.core.text.CharPool.DOT;

/**
 * 定制的Qos消费者
 * <T> 消息内容类型
 *
 * @author QIUCHANGQING620
 * @date 2020-05-27 20:23
 */
@Slf4j
public class RabbitCustomizedConsumer<T> implements Runnable {

    private static final String PREFIX = "rabbitmq-";

    private static final String SUFFIX = "-config";

    private RabbitTemplate rabbitTemplate;

    /**
     * 缓存消息的队列
     */
    private BlockingDeque<RabbitMqMessage<T>> blockingDeque = new LinkedBlockingDeque<>();

    /**
     * 缓存订阅者标签的队列
     */
    private Deque<String> consumerTagDeque = new ArrayDeque<>();

    /**
     * Mq环境配置
     */
    private RabbitContext rabbitContext;

    /**
     * 交换机
     */
    private String exchange;

    /**
     * 队列
     */
    private String queue;

    /**
     * 单次消费最大数量
     */
    private int prefetchCount;

    /**
     * 处理线程最大数
     */
    private int maxConcurrency;

    /**
     * 订阅者ip
     */
    private String ips;

    /**
     * 当前机器是否为订阅者机器
     */
    private boolean isHit;

    /**
     * 消息处理方法
     */
    private Consumer<List<T>> function;

    /**
     * 消息反序列化类
     */
    private Class<T> tClass;

    private static final ObjectMapper OBJECT_MAPPER = JacksonUtil.copy();

    public RabbitCustomizedConsumer(RabbitTemplate rabbitTemplate, RabbitContext rabbitContext,
                                    Consumer<List<T>> function, Class<T> clazz) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = rabbitContext.getExchange();
        this.queue = rabbitContext.getQueue();
        this.rabbitContext = rabbitContext;
        this.prefetchCount = rabbitContext.getPrefetchCount();
        this.maxConcurrency = rabbitContext.getMaxConcurrency();
        this.ips = JacksonUtil.bean2Json(rabbitContext.getIps());
        this.function = function;
        this.tClass = clazz;
        this.isHit = CollectionUtils.isEmpty(rabbitContext.getIps()) || IpUtil.isIpInCol(rabbitContext.getIps());
    }

    @Override
    public void run() {
        // 实例化线程池
        ScheduledThreadPoolExecutor executor = new ThreadPoolUtil.ScheduledThreadPoolExecutorMDCWrapper(this.maxConcurrency,
                ThreadFactoryBuilder.create().setNamePrefix(PREFIX + this.queue + DASHED).build());

        rabbitTemplate.execute(channel -> {
            try {
                // 配置变更控制线程
                Thread consumerThread = new ThreadPoolUtil.ThreadMDCWrapper(() -> {
                    while (true) {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            log.error("queue : [" + queue + "] config consumers is interrupt", e);
                            break;
                        }
                        configConsumers(channel);
                        refreshMaxConcurrency(executor);
                    }
                }, this.queue + SUFFIX);
                consumerThread.start();

                while (true) {
                    // 等待订阅者从 mq 中获取一定数量消息到 blockingDeque 中
                    try {
                        TimeUnit.MILLISECONDS.sleep(this.rabbitContext.getWaitTime());
                    } catch (InterruptedException e) {
                        log.error("queue : [" + this.queue + "] custom consumer is interrupt", e);
                        break;
                    }

                    while (this.blockingDeque.size() > 0) {
                        // 尝试从 blockingDeque 中提取消息
                        List<RabbitMqMessage<T>> list = new ArrayList<>();
                        int size = this.blockingDeque.drainTo(list, this.prefetchCount);

                        if (size > 0) {
                            // 提取出来数量大于0，则启动消费者进程
                            executor.execute(() -> invokeConsumeFunction(channel, list));
                        }
                    }
                }
            } catch (Exception e) {
                log.error("running queue : [" + this.queue + "] listener fail", e);
            }
            return null;
        });
    }

    /**
     * 配置订阅者
     *
     * @param channel mq通道
     */
    private void configConsumers(Channel channel) {
        // 是否为订阅者机器
        Set<String> nowIps = this.rabbitContext.getIps();
        if (!this.ips.equals(JacksonUtil.bean2Json(nowIps))) {
            log.info("queue : [{}] ip change", this.queue);
            this.ips = JacksonUtil.bean2Json(nowIps);
            this.isHit = CollectionUtils.isEmpty(nowIps) || IpUtil.isIpInCol(nowIps);
        }
        // 是否已订阅
        boolean isReceiver = this.consumerTagDeque.size() != 0;
        // 当前配置的处理线程最大数
        int nowMaxConcurrency = this.rabbitContext.getMaxConcurrency();
        // 当前配置的单次消费最大数量
        int nowPrefetchCount = this.rabbitContext.getPrefetchCount();
        // 是否更换处理线程最大数
        boolean isChangeMaxConcurrency = this.maxConcurrency != nowMaxConcurrency;
        // 单次消费最大数量
        boolean isChangePrefetchCount = this.prefetchCount != nowPrefetchCount;

        // - 订阅者机器&未订阅 ：初始化订阅者
        // - 订阅者机器&已订阅&PrefetchCount变更 ： 重新初始化所有订阅
        // - 订阅者机器&已订阅&MaxConcurrency变更 ： 注销/注册部分订阅者
        // - 非订阅者机器&已订阅 ：取消订阅
        // - 其他 ：无需操作
        try {
            if (this.isHit && !isReceiver) {
                log.info("queue : [{}] consumers register. maxConcurrency : [{}], prefetchCount : [{}]",
                        this.queue, nowMaxConcurrency, nowPrefetchCount);
                registerConsumer(channel, nowMaxConcurrency, nowPrefetchCount);

                this.maxConcurrency = nowMaxConcurrency;
                this.prefetchCount = nowPrefetchCount;
            } else if (this.isHit && isChangePrefetchCount) {
                log.info("queue : [{}] prefetchCount change. maxConcurrency : [{}], prefetchCount : [{}]",
                        this.queue, nowMaxConcurrency, nowPrefetchCount);
                cancelConsumer(channel, 0);
                registerConsumer(channel, nowMaxConcurrency, nowPrefetchCount);

                this.maxConcurrency = nowMaxConcurrency;
                this.prefetchCount = nowPrefetchCount;
            } else if (this.isHit && isChangeMaxConcurrency) {
                log.info("queue : [{}] maxConcurrency change to [{}]", this.queue, this.maxConcurrency);
                if (this.maxConcurrency > nowMaxConcurrency) {
                    cancelConsumer(channel, nowMaxConcurrency);
                } else {
                    registerConsumer(channel, nowMaxConcurrency, nowPrefetchCount);
                }

                this.maxConcurrency = nowMaxConcurrency;
            } else if (!this.isHit && isReceiver) {
                log.info("queue : [{}] consumers cancel", this.queue);
                cancelConsumer(channel, 0);
            }
        } catch (IOException e) {
            log.error("config queue : [" + this.queue + "] consumers fail", e);
        }
    }

    /**
     * 注册订阅者
     *
     * @param channel           mq通道
     * @param nowMaxConcurrency 处理线程最大数
     * @param nowPrefetchCount  单次消费最大数量
     * @throws IOException IO异常
     */
    private void registerConsumer(Channel channel, int nowMaxConcurrency, int nowPrefetchCount) throws IOException {
        // 设定每批次从mq接收的消息数量
        channel.basicQos(nowPrefetchCount, false);
        if (log.isDebugEnabled()) {
            log.debug("set queue : [{}] qos : [{}]", this.queue, nowPrefetchCount);
        }
        while (this.consumerTagDeque.size() != nowMaxConcurrency) {
            // 注册订阅者
            String consumerTag = channel.basicConsume(this.exchange + DOT + this.queue, false, initConsumer(channel));
            this.consumerTagDeque.add(consumerTag);
            log.info("register queue : [{}] consumerTag : [{}]", this.queue, consumerTag);
        }
    }

    /**
     * 注销订阅者
     *
     * @param channel   mq通道
     * @param targetNum 剩余订阅者数量
     * @throws IOException IO异常
     */
    private void cancelConsumer(Channel channel, int targetNum) throws IOException {
        while (this.consumerTagDeque.size() != targetNum) {
            // 注销订阅者
            String consumerTag = this.consumerTagDeque.pollLast();
            channel.basicCancel(consumerTag);
            log.info("cancel queue : [{}] consumerTag : [{}]", this.queue, consumerTag);
        }
    }

    /**
     * 初始化订阅者
     *
     * @param channel mq通道
     * @return 订阅者
     */
    private DefaultConsumer initConsumer(Channel channel) {
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                long tag = envelope.getDeliveryTag();
                String messageId = properties.getMessageId();
                if (log.isDebugEnabled()) {
                    log.debug("Received： " + msg + ", deliveryTag: " + tag + ", messageId: " + messageId);
                }
                T t;
                if (tClass == String.class) {
                    t = (T) msg;
                } else {
                    try {
                        t = OBJECT_MAPPER.readValue(msg, tClass);
                    } catch (JsonProcessingException e) {
                        t = OBJECT_MAPPER.readValue(OBJECT_MAPPER.readValue(msg, String.class), tClass);
                    }
                }

                blockingDeque.add(new RabbitMqMessage<>(tag, t));
            }
        };
    }

    /**
     * 刷新消费者最大线程数
     *
     * @param executor 线程池
     */
    private void refreshMaxConcurrency(ThreadPoolExecutor executor) {
        if (executor.getCorePoolSize() != this.maxConcurrency) {
            log.info("queue : [{}] ThreadPoolExecutor change to : [{}]", this.queue, this.maxConcurrency);
            executor.setCorePoolSize(this.maxConcurrency);
        }
    }

    /**
     * 调用实际消费者方法
     *
     * @param channel mq通道
     * @param list    要处理的消息列表
     */
    private void invokeConsumeFunction(Channel channel, List<RabbitMqMessage<T>> list) {
        if (rabbitContext.isConsumerLog()) {
            log.info("get {} messages : {}", list.size(), JacksonUtil.bean2Json(list));
        }

        boolean isSuccess = true;
        try {
            // 调用实际消费者
            this.function.accept(list.stream().map(RabbitMqMessage::getBody).collect(Collectors.toList()));
        } catch (Exception e) {
            isSuccess = false;
            log.error("invoke queue : [" + this.queue + "] consumer fail", e);
        }

        // 向 mq 发送 ack
        boolean finalIsSuccess = isSuccess;
        for (RabbitMqMessage<T> message : list) {
            try {
                if (finalIsSuccess) {
                    channel.basicAck(message.getDeliveryTag(), false);
                } else {
                    channel.basicReject(message.getDeliveryTag(), true);
                }
            } catch (IOException e) {
                log.error("ack queue : [" + this.queue + "], deliveryTag : [" + message.getDeliveryTag() + "] fail -> {} ",
                        JacksonUtil.bean2Json(message.getBody()), e);
            }
        }
    }

}
