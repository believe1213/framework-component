package com.minister.framework.debug.kafka.prop;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * kafka调试管理开关
 *
 * @author QIUCHANGQING620
 * @date 2024-05-04 00:00
 */
@Component
@Data
public class KafkaSwitchConfig {

    /**
     * 灰度节点消费kafka消息开关
     * 关闭后，灰度节点不会监听消费kafka消息，无法使用kafka消息灰度功能
     */
    @Value("${service.gray.kafka.enable:true}")
    private boolean enableKafkaGray;

    /**
     * 不参与灰度的kafka集群
     */
    @Value("${service.gray.kafka.exclude-bootstrap-servers:''}")
    private String grayExcludeServer;

}
