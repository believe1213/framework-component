package com.minister.component.kafka.properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.stereotype.Component;

/**
 * kafka默认配置项
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 11:35
 */
@Component
public class KafkaDefaultProperties {

    private static Environment environment;

    public KafkaDefaultProperties(Environment env) {
        KafkaDefaultProperties.environment = env;
    }

    // ---------- common ----------

    public static String getBootstrapServers() {
        String bootstrapServers = environment.getProperty("spring.kafka.bootstrap-servers");
        if (StringUtils.isBlank(bootstrapServers)) {
            throw new IllegalArgumentException("Could not resolve property 'spring.kafka.bootstrap-servers'");
        }
        return bootstrapServers;
    }

    // ---------- producer ----------

    public static String getTransactionIdPrefix() {
        return environment.getProperty("spring.kafka.producer.transaction-id-prefix", "KafkaTx-");
    }

    public static String getAcks() {
        return environment.getProperty("spring.kafka.producer.acks", "1");
    }

    public static String getRetries() {
        return environment.getProperty("spring.kafka.producer.retries", "3");
    }

    public static String getMaxRequestSize() {
        return environment.getProperty("spring.kafka.properties.max-request-size", StringUtils.EMPTY);
    }

    public static Integer getRequestTimeout() {
        return environment.getProperty("spring.kafka.properties.request-timeout", Integer.class, 5000);
    }

    public static Integer getDeliveryTimeout() {
        return environment.getProperty("delivery.timeout.ms", Integer.class, 10000);
    }

    public static String getBatchSize() {
        return environment.getProperty("spring.kafka.producer.batch-size", "16384");
    }

    public static String getLingerMs() {
        return environment.getProperty("spring.kafka.properties.linger-ms", "200");
    }

    public static String getBufferMemory() {
        return environment.getProperty("spring.kafka.producer.buffer-memory", "1024000");
    }

    // ---------- consumer ----------

    public static String getGroupId() {
        return environment.getProperty("spring.kafka.consumer.group-id", StringUtils.EMPTY);
    }

    public static Boolean getEnableAutoCommit() {
        return environment.getProperty("spring.kafka.consumer.enable-auto-commit", Boolean.class, false);
    }

    public static String getAutoCommitInterval() {
        return environment.getProperty("spring.kafka.consumer.auto-commit-interval", "2000");
    }

    public static String getAutoOffsetReset() {
        return environment.getProperty("spring.kafka.consumer.auto-offset-reset", "latest");
    }

    public static String getMaxPollIntervalMs() {
        return environment.getProperty("spring.kafka.properties.max-poll-interval", "300000");
    }

    public static String getMaxPollRecords() {
        return environment.getProperty("spring.kafka.consumer.max-poll-records", "100");
    }

    public static String getSessionTimeout() {
        return environment.getProperty("spring.kafka.properties.session-timeout", "30000");
    }

    public static Integer getConcurrency() {
        return environment.getProperty("spring.kafka.listener.concurrency", Integer.class, 3);
    }

    public static Boolean getMissingTopicsFatal() {
        return environment.getProperty("spring.kafka.listener.missing-topics-fatal", Boolean.class, false);
    }

    public static Long getPollTimeout() {
        return environment.getProperty("spring.kafka.listener.poll-timeout", Long.class, 600000L);
    }

    public static ContainerProperties.AckMode getAckMode() {
        return environment.getProperty("spring.kafka.listener.ack-mode", ContainerProperties.AckMode.class,
                ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    }

    public static Boolean getAutoStartup() {
        return environment.getProperty("spring.kafka.streams.auto-startup", Boolean.class, true);
    }

    public static String getIsolationLevel() {
        return environment.getProperty("spring.kafka.consumer.isolation.level", "read_committed");
    }

}
