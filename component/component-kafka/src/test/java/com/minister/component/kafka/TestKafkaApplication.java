package com.minister.component.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * TestKafkaApplication
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 17:40
 */
@ComponentScan(basePackages = "com.minister")
@SpringBootApplication(scanBasePackages = "com.minister")
public class TestKafkaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestKafkaApplication.class, args);
    }

}
