package com.minister.framework.debug.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.Ordered;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * kafka启动和停止
 *
 * @author QIUCHANGQING620
 * @date 2024-05-04 00:00
 */
@Slf4j
@Component
public class CustomKafkaStartAndStop implements ApplicationRunner, ApplicationListener<ContextClosedEvent>, Ordered {

    @Resource
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    /**
     * 启动kafka监听消费
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始启动kafka监听消费");
        for (MessageListenerContainer listenerContainer : kafkaListenerEndpointRegistry.getListenerContainers()) {
            if (!listenerContainer.isRunning()) {
                listenerContainer.start();
            }
        }
        log.info("启动kafka监听消费完成");
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("开始停止kafka监听消费");
        kafkaListenerEndpointRegistry.stop();
        log.info("停止kafka监听消费完成");
    }

    @Override
    public int getOrder() {
        return 35;
    }

}
