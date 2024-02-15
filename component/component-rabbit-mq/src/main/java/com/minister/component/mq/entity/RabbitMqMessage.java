package com.minister.component.mq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Mq的Message预处理对象
 *
 * @author QIUCHANGQING620
 * @date 2020-05-29 17:03
 */
@AllArgsConstructor
@Data
public class RabbitMqMessage<T> {

    /**
     * 交付标签
     */
    private Long deliveryTag;

    /**
     * 消息内容
     */
    private T body;

}
