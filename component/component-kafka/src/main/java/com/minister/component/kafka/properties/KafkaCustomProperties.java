package com.minister.component.kafka.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

/**
 * kafka定制化配置项
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 11:35
 */
@Component
@ConfigurationProperties(prefix = "kafka")
@Data
public class KafkaCustomProperties {

    private Map<String, Properties> producer;

    private Map<String, Properties> consumer;

}
