package com.minister.component.mq.util;

import cn.hutool.extra.spring.SpringUtil;
import com.minister.component.mq.entity.RabbitContext;
import com.minister.component.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.Map;

import static cn.hutool.core.text.CharPool.DASHED;

/**
 * 生产工具类
 *
 * @author QIUCHANGQING620
 * @date 2020-09-04 10:39
 */
@AutoConfigureAfter(RabbitTemplate.class)
@Component
@Import(SpringUtil.class)
@Slf4j
public class RabbitProducerUtil {

    private static RabbitTemplate rabbitTemplate;

    private RabbitContext rabbitContext;

    private RabbitProducerUtil() {
    }

    private RabbitProducerUtil(RabbitContext rabbitContext) {
        this.rabbitContext = rabbitContext;
    }

    public static RabbitProducerUtil instance(RabbitContext context) {
        RabbitProducerUtil.rabbitTemplate = SpringUtil.getBean("rabbitTemplate");
        return new RabbitProducerUtil(context);
    }

    /**
     * 向mq发送消息
     *
     * @param obj 对象
     */
    public void convertAndSend(Object obj) {
        String str = obj instanceof String ? (String) obj : JacksonUtil.bean2Json(obj);
        rabbitTemplate.convertAndSend(rabbitContext.getExchange(), rabbitContext.getRoutingKey(), str);
    }

    /**
     * 向mq发送延迟消息
     *
     * @param obj      对象
     * @param delayNum 延迟队列序号
     */
    public void convertAndSendDelay(Object obj, int delayNum) {
        if (!RabbitDelayUtil.isDlx(rabbitContext)) {
            log.error("can not get delay config");
            return;
        }
        if (delayNum <= 0) {
            log.error("delayNum has to be greater than zero");
            return;
        }
        Map<Integer, Long> delayNum2TimeMap = rabbitContext.getDelayNum2TimeMap();
        delayNum = Math.min(delayNum, delayNum2TimeMap.size());
        String str = obj instanceof String ? (String) obj : JacksonUtil.bean2Json(obj);
        rabbitTemplate.convertAndSend(rabbitContext.getDelayExchange() + DASHED + delayNum,
                rabbitContext.getDelayRoutingKey(),
                str,
                RabbitDelayUtil.delay(delayNum2TimeMap.get(delayNum)));
    }

}
