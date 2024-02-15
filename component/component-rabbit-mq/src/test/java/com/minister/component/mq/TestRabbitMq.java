package com.minister.component.mq;

import cn.hutool.core.date.DateUtil;
import com.minister.component.mq.dto.TestDelayDto;
import com.minister.component.mq.entity.RabbitContext;
import com.minister.component.mq.entity.RabbitContextBuilder;
import com.minister.component.mq.util.RabbitConsumerUtil;
import com.minister.component.mq.util.RabbitProducerUtil;
import com.minister.component.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TestRabbitMq
 *
 * @author QIUCHANGQING620
 * @date 2020-02-18 12:06
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestRabbitMqApplication.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestRabbitMq {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RabbitConsumerUtil rabbitConsumerUtil;

    /**
     * 延迟队列环境配置
     */
    private static RabbitContext rabbitContext = RabbitContextBuilder.instance()
            .topic("componentTestExchange", "componentTestQueue", 3, 1)
            .delay("componentTestDelayExchange", "componentTestDelayQueue", new Long[]{3L, 5L})
            .build();

    @Test
    public void t1DelayConsumer() throws InterruptedException {
        // 启动自定义消费者
        rabbitConsumerUtil.consume(rabbitContext, this::tDelayConsumer, TestDelayDto.class);

        // 初始化生产者
        RabbitProducerUtil rabbitProducerUtil = RabbitProducerUtil.instance(rabbitContext);
        // 发送消息
        rabbitProducerUtil.convertAndSend(new TestDelayDto(0, 0, DateUtil.formatDateTime(new Date())));
        TimeUnit.SECONDS.sleep(5);
        TestDelayDto testDelayDto = new TestDelayDto(1, 0, DateUtil.formatDateTime(new Date()));
        rabbitProducerUtil.convertAndSend(testDelayDto);
        TimeUnit.SECONDS.sleep(20);
    }

    public void tDelayConsumer(List<TestDelayDto> msgList) {
        Date nowDate = new Date();
        Date consumeTimeMin = DateUtils.addSeconds(nowDate, -3);

        RabbitProducerUtil rabbitProducerUtil = RabbitProducerUtil.instance(rabbitContext);

        for (TestDelayDto msg : msgList) {
            log.info(JacksonUtil.bean2Json(msg));
            Date consumeTime = DateUtil.parseDateTime(msg.getConsumeTime());
            boolean isExpect = DateUtil.compare(consumeTimeMin, consumeTime) <= 0 && DateUtil.compare(nowDate, consumeTime) >= 0;
            Assert.assertTrue(isExpect);
            int retryNum = msg.getRetryNum() + 1;
            if (retryNum > 3) {
                continue;
            }
            msg.setRetryNum(retryNum);
            retryNum = Math.min(retryNum, rabbitContext.getDelayCount());
            Date consumeDate = DateUtils.addSeconds(nowDate, rabbitContext.getDelayTime(retryNum).intValue());
            msg.setConsumeTime(DateUtil.formatDateTime(consumeDate));

            rabbitProducerUtil.convertAndSendDelay(msg, retryNum);
        }
    }

    /**
     * 延迟队列环境配置
     */
    private static RabbitContext rabbitContext2 = RabbitContextBuilder.instance()
            .topic("componentTestExchange2", "componentTestQueue2", 5, 2)
            .build();

    private static AtomicInteger num;

    @Test
    public void t2BatchConsumer() throws InterruptedException {
        // 初始化生产者
        RabbitProducerUtil rabbitProducerUtil = RabbitProducerUtil.instance(rabbitContext2);
        for (int i = 1; i <= 100; i++) {
            TestDelayDto testDelayDto = new TestDelayDto(i, 0, null);
            rabbitProducerUtil.convertAndSend(testDelayDto);
        }
        TimeUnit.SECONDS.sleep(3);
        num = new AtomicInteger(0);
        // 启动自定义消费者
        rabbitConsumerUtil.consume(rabbitContext2, this::tBatchConsumer, TestDelayDto.class);

        TimeUnit.SECONDS.sleep(15);
        Assert.assertEquals(100, num.get());
    }

    public void tBatchConsumer(List<TestDelayDto> msgList) {
        Assert.assertEquals(5, msgList.size());
        num.addAndGet(5);
        long threadId = Thread.currentThread().getId();
        System.out.println("Thread id : [" + threadId + "] sleep start");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            System.out.println("Thread id : [" + threadId + "] interrupted");
        }
        System.out.println("Thread id : [" + threadId + "] sleep end");
    }


    @Test
    public void t3TestConfirm() {
        /*
         * 设置消息确认回调方法
         *
         * @ack 为true时，表示投递成功；为false表示投递失败
         * @CorrelationData 为自定义反馈信息
         * @cause 为投递失败的原因
         */
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("ack：" + ack);
                if (!ack) {
                    System.out.println("correlationData：" + correlationData);
                    System.out.println("cause：" + cause);
                }
            }
        });

        // 消息内容
        Map<String, String> map = new HashMap<>(0);
        map.put("message", "testing confirm function");

        // 设置自定义反馈消息
        String uuid = UUID.randomUUID().toString();
        System.out.println("消息唯一ID：" + uuid);
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(uuid);

        try {
            // 并不存在名为“exchange-component-test”的exchange
            rabbitTemplate.convertAndSend("exchange-component-test", "test", map, correlationData);
        } catch (Exception e) {
            // 消息唯一ID：e6601e83-fad7-4b53-9968-c74828e62b23
            // ack：false
            // correlationData：CorrelationData [id=e6601e83-fad7-4b53-9968-c74828e62b23]
            // cause：channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no exchange 'exchange-component-test' in vhost '/', class-id=60, method-id=40)
            System.out.println("testing confirm function convertAndSend fail");
        }
    }

    @Test
    public void t4TestReturn() {
        /*
         * 设置消息返回回调方法
         * 该方法执行时则表示消息投递失败
         *
         * @message 为反馈信息
         * @replyCode 一个反馈代码，表示不同投递失败原因
         * @replyText 反馈信息
         */
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("返回消息配置：" + message.getMessageProperties().toString());
                System.out.println("反馈代码：" + replyCode);
                System.out.println("反馈内容：" + replyText);
                System.out.println("exchange：" + exchange);
                System.out.println("routingKey：" + routingKey);
            }
        });

        // 消息内容
        Map<String, String> map = new HashMap<>();
        map.put("message", "testing return function");

        // 并不存在名为“test”的routingKey，即投不到现有的queue里
        try {
            rabbitTemplate.convertAndSend("exchange-component-test", "test", map);
        } catch (Exception e) {
            // 返回消息配置：MessageProperties [headers={}, contentType=application/x-java-serialized-object, contentLength=0, receivedDeliveryMode=PERSISTENT, priority=0, deliveryTag=0]
            // 反馈代码：312
            // 反馈内容：NO_ROUTE
            // exchange：exchange-component-test
            // routingKey：test
            System.out.println("testing return function convertAndSend fail");
        }
    }

    @Test
    public void testReentrantLock() {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        Thread t1 = new Thread(() -> {
            System.out.println("[" + DateUtil.now() + "] " + Thread.currentThread().getName() + " start");
            try {
                lock.lock();
                System.out.println("[" + DateUtil.now() + "] " + Thread.currentThread().getName() + " t1 lock");
                condition.await();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
                System.out.println("[" + DateUtil.now() + "] " + Thread.currentThread().getName() + " t1 unlock");
            }
        });

        Thread t2 = new Thread(() -> {
            System.out.println("[" + DateUtil.now() + "] " + Thread.currentThread().getName() + " start");
            try {
                lock.lock();
                System.out.println("[" + DateUtil.now() + "] " + Thread.currentThread().getName() + " t2 lock");
                condition.signal();
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
                System.out.println("[" + DateUtil.now() + "] " + Thread.currentThread().getName() + " t2 unlock");
            }
        });

        t1.start();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        t2.start();
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
