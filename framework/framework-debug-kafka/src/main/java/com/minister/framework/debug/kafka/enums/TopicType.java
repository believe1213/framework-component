package com.minister.framework.debug.kafka.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Topic类型
 *
 * @author QIUCHANGQING620
 * @date 2024-05-04 00:00
 */
@AllArgsConstructor
@Getter
public enum TopicType {

    /**
     * 原始topic
     */
    ORIGINAL("本地服务原始topic"),

    /**
     * 远程调试topic
     */
    DEBUG("本地服务远程调试topic"),

    /**
     * 泳道topic
     */
    MULTI_VERSION("泳道topic");

    private String type;

}
