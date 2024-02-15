package com.minister.component.kafka;

import com.minister.component.kafka.consumer.AbstractKafkaConsumer;
import com.minister.component.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TestKafkaConsumer
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 17:50
 */
@Component
@Slf4j
public class TestKafkaConsumer extends AbstractKafkaConsumer<TestMsg> {

    /**
     * 订阅消息
     *
     * @param records 消息列表，每条消息是字符串格式，可以是json对象
     */
    @KafkaListener(topics = TestTopic.TEST_TOPIC, containerFactory = TestTopic.TEST_TOPIC)
    protected void onReceiveMessage(List<String> records, Acknowledgment ack) {
        this.processReceiveMessage(records, ack, TestMsg.class);
    }

    /**
     * 消息处理
     *
     * @param messageList 消息列表
     * @return 是否消费成功
     */
    @Override
    protected boolean consumeMessage(List<TestMsg> messageList) {
        log.info("consumeMessage : {}", JacksonUtil.bean2Json(messageList));
        return true;
    }

}
