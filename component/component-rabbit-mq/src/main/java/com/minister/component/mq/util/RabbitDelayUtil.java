package com.minister.component.mq.util;

import com.minister.component.mq.entity.RabbitContext;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;

import java.util.concurrent.TimeUnit;

/**
 * 延迟发送工具类
 *
 * @author QIUCHANGQING620
 * @date 2020-06-22 16:11
 */
public class RabbitDelayUtil {

    private RabbitDelayUtil() {
    }

    /**
     * 延迟发送工具类
     *
     * @param delayTime 延迟秒数
     * @return MessagePostProcessor
     */
    public static MessagePostProcessor delay(Long delayTime) {
        if (delayTime == null || delayTime <= 0) {
            throw new IllegalArgumentException("delayTime has to be greater than zero");
        }
        return message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            message.getMessageProperties().setExpiration(String.valueOf(TimeUnit.MILLISECONDS.convert(delayTime, TimeUnit.SECONDS)));
            return message;
        };
    }

    /**
     * 延迟发送工具类
     *
     * @param delayTime 延迟时间
     * @param unit      时间单位
     * @return MessagePostProcessor
     */
    public static MessagePostProcessor delay(Long delayTime, TimeUnit unit) {
        if (delayTime == null || delayTime <= 0) {
            throw new IllegalArgumentException("delayTime has to be greater than zero");
        }
        if (unit == null) {
            throw new IllegalArgumentException("unit can not be null");
        }
        return messageProcessor -> {
            MessageProperties messageProperties = messageProcessor.getMessageProperties();
            messageProperties.setExpiration(String.valueOf(TimeUnit.MILLISECONDS.convert(delayTime, unit)));
            return messageProcessor;
        };
    }

    /**
     * 判断是否满足延迟队列配置
     *
     * @param rabbitContext 环境配置信息
     * @return 是否满足延迟队列配置
     */
    public static boolean isDlx(RabbitContext rabbitContext) {
        boolean hasDelayExchange = StringUtils.isNotBlank(rabbitContext.getDelayExchange());
        boolean hasDelayQueue = StringUtils.isNotBlank(rabbitContext.getDelayQueue());
        boolean hasDelayTime = MapUtils.isNotEmpty(rabbitContext.getDelayNum2TimeMap());
        return hasDelayExchange && hasDelayQueue && hasDelayTime;
    }

}
