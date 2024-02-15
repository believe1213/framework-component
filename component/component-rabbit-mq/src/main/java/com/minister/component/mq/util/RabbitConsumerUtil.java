package com.minister.component.mq.util;

import com.minister.component.mq.RabbitCustomizedConsumer;
import com.minister.component.mq.entity.RabbitContext;
import com.minister.component.trace.utils.ThreadPoolUtil;
import com.rabbitmq.client.AMQP;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cn.hutool.core.text.CharPool.DASHED;
import static cn.hutool.core.text.CharPool.DOT;

/**
 * 消费者工具类
 *
 * @author QIUCHANGQING620
 * @date 2020-6-11 21:59
 */
@Component
public class RabbitConsumerUtil {

    private static final String DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";

    private static final String DEAD_LETTER_ROUTING = "x-dead-letter-routing-key";

    private static final String SUFFIX = "-consumer";

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 自定义Qos消费者
     *
     * @param rabbitContext 消费上下文
     * @param function      实际消费者方法
     * @param clazz         消息内容类型
     * @param <T>           消息内容类型
     * @return 消费者线程
     */
    public <T> Thread consume(RabbitContext rabbitContext, Consumer<List<T>> function, Class<T> clazz) {
        // 参数校验
        validate(rabbitContext);

        // 交换机和队列初始化
        rabbitTemplate.execute((ChannelCallback<String>) channel -> {
            if (!rabbitContext.isDeclareAndBind()) {
                return null;
            }
            // durable : 是否持久化(默认:true)
            // exclusive : 是否排他(默认:false)
            // autoDelete : 是否自动删除(默认:false)
            // internal : 是否为内部使用,即不允许客户端调用(默认:false)
            String queue = rabbitContext.getCustomQueue() ? rabbitContext.getQueue() : rabbitContext.getExchange() + DOT + rabbitContext.getQueue();
            AMQP.Exchange.DeclareOk exchangeDeclareOk = channel.exchangeDeclare(rabbitContext.getExchange(), rabbitContext.getExchangeType(), true, false, false, null);
            AMQP.Queue.DeclareOk queueDeclareOk = channel.queueDeclare(queue, true, false, false, null);
            AMQP.Queue.BindOk bindOk = channel.queueBind(queue, rabbitContext.getExchange(), rabbitContext.getRoutingKey());

            /*
             * 设置死信队列
             */
            if (RabbitDelayUtil.isDlx(rabbitContext)) {
                Map<String, Object> args = new HashMap<>(2);
                args.put(DEAD_LETTER_EXCHANGE, rabbitContext.getExchange());
                args.put(DEAD_LETTER_ROUTING, rabbitContext.getRoutingKey());
                int delayNum = rabbitContext.getDelayNum2TimeMap().size() + 1;
                while (--delayNum > 0) {
                    String delayExchange = rabbitContext.getDelayExchange() + DASHED + delayNum;
                    String delayQueue = rabbitContext.getDelayQueue() + DASHED + delayNum;
                    delayQueue = delayExchange + DOT + delayQueue;
                    channel.exchangeDeclare(delayExchange, rabbitContext.getDelayExchangeType(), true, false, false, null);
                    channel.queueDeclare(delayQueue, true, false, false, args);
                    channel.queueBind(delayQueue, delayExchange, rabbitContext.getDelayRoutingKey());
                }
            }

            return null;
        });
        // 实例化自定义Qos消费者
        RabbitCustomizedConsumer<T> consumer = new RabbitCustomizedConsumer<>(
                rabbitTemplate, rabbitContext, function, clazz);

        Thread consumerThread = new ThreadPoolUtil.ThreadMDCWrapper(consumer, rabbitContext.getQueue() + SUFFIX);
        consumerThread.setDaemon(true);
        consumerThread.start();

        return consumerThread;
    }

    /**
     * 参数校验
     *
     * @param rabbitContext 环境配置信息p
     */
    private void validate(RabbitContext rabbitContext) {
        if (StringUtils.isBlank(rabbitContext.getExchange())) {
            throw new IllegalArgumentException("exchange can not be null");
        }
        if (StringUtils.isBlank(rabbitContext.getQueue())) {
            throw new IllegalArgumentException("queue can not be null");
        }
        if (rabbitContext.getPrefetchCount() == null || rabbitContext.getPrefetchCount() <= 0) {
            throw new IllegalArgumentException("prefetchCount has to be greater than zero");
        }
        if (rabbitContext.getMaxConcurrency() == null || rabbitContext.getMaxConcurrency() <= 0) {
            throw new IllegalArgumentException("maxConcurrency has to be greater than zero");
        }
        if (rabbitContext.getWaitTime() <= 0L) {
            throw new IllegalArgumentException("waitTime has to be greater than or equal zero");
        }
    }

}
