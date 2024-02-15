package com.minister.component.kafka.exception;

import com.minister.component.utils.enums.FrameworkExEnum;
import com.minister.component.utils.exception.BaseException;

/**
 * kafka异常
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 13:15
 */
public class KafkaException extends BaseException {

    public KafkaException() {
        super(FrameworkExEnum.MQ_EX);
    }

    public KafkaException(Throwable e) {
        super(FrameworkExEnum.MQ_EX, e);
    }

}
