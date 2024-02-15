package com.minister.component.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 数据延迟Dto
 *
 * @author QIUCHANGQING620
 * @date 2020-08-19 18:50
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestDelayDto implements Serializable {

    /**
     * 消息序号
     */
    private Integer msgNo;

    /**
     * 重试次数
     */
    private int retryNum = 0;

    /**
     * 预估消费时间
     */
    private String consumeTime;

}
