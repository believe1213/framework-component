package com.minister.framework.debug.core.local.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 本地调试类型
 *
 * @author QIUCHANGQING620
 * @date 2024-05-01 00:00
 */
@AllArgsConstructor
@Getter
public enum DebugType {

    MOBILE(2, "手机号"),
    URI(3, "URI相对路径"),
    TOPIC(4, "kafka topic"),
    QUEUE(5, "mq queue"),
    ROCKETMQ(6, "rocketmq topic:tag"),
    ;

    private Integer type;
    private String msg;

}
