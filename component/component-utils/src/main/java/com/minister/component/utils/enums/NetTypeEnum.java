package com.minister.component.utils.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

/**
 * 网络类型enum
 *
 * @author QIUCHANGQING620
 * @date 2020-02-21 23:22
 */
public enum NetTypeEnum {

    /**
     * 未知网络
     */
    UNKNOWN,
    WIFI,
    /**
     * 2G网络
     */
    M2G,
    M3G,
    M4G,
    M5G,
    ;

    @JsonCreator
    public static NetTypeEnum getNetType(String name) {
        return Arrays.stream(values()).filter(it -> it.name().equalsIgnoreCase(name)).findFirst().orElse(UNKNOWN);
    }

}
