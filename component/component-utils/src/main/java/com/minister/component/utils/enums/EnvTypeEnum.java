package com.minister.component.utils.enums;

import java.util.Arrays;

/**
 * 当前环境枚举
 *
 * @author QIUCHANGQING620
 * @date 2020-9-2 16:46
 */
public enum EnvTypeEnum {

    /**
     * 环境标识
     */
    DEV,
    STG,
    PROD,
    ;

    /**
     * 获取环境信息
     */
    public static EnvTypeEnum getByName(String env) {
        return Arrays.stream(values()).filter(it -> it.name().equalsIgnoreCase(env)).findFirst().orElse(null);
    }

}
