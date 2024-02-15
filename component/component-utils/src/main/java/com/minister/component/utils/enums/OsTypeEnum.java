package com.minister.component.utils.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

/**
 * 客户端系统类型enum
 *
 * @author QIUCHANGQING620
 * @date 2020-03-02 13:20
 */
public enum OsTypeEnum {

    /**
     * 未知系统类型
     */
    UNKNOWN,
    MINI_PROGRAM,
    H5,
    ANDROID,
    IOS,
    ;

    @JsonCreator
    public static OsTypeEnum getOsType(String name) {
        return Arrays.stream(values()).filter(it -> it.name().equalsIgnoreCase(name)).findFirst().orElse(UNKNOWN);
    }

}
