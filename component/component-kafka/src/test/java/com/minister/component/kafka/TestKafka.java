package com.minister.component.kafka;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.minister.component.kafka.utils.KafkaProducerUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * TestKafka
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 17:47
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestKafkaApplication.class)
@Slf4j
public class TestKafka {

    /**
     * 测试发送消息和订阅消息
     */
    @Test
    public void t() throws InterruptedException {
        // 本地消费需要添加VM参数：-DRUN.MODEL=LOCAL
        Date nowDate = new Date();
        for (int i = 1; i <= 2; i++) {
            String dateStr = DateUtil.format(nowDate, DatePattern.NORM_DATETIME_PATTERN);
            TestMsg testMessage = TestMsg.builder().name("name-" + i).time(dateStr).build();
            KafkaProducerUtil.sendMessageSync(TestTopic.TEST_TOPIC, testMessage);
            nowDate = DateUtils.addSeconds(nowDate, 1);
        }
        log.info("------");
        TimeUnit.SECONDS.sleep(3);
    }

}
