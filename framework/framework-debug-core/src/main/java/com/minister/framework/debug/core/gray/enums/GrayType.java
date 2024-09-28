package com.minister.framework.debug.core.gray.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 灰度类型
 *
 * @author QIUCHANGQING620
 * @date 2024-05-03 00:00
 */
@AllArgsConstructor
@Getter
public enum GrayType {

    MOBILE(2, "手机号"),
    URI(3, "URI相对路径"),
    TOPIC(4, "kafka topic"),
    QUEUE(5, "mq queue"),
    ROCKETMQ(6, "rocketmq topic:tag"),
    HEADER(12, "header头"),
    ;

    private Integer type;
    private String msg;

}
