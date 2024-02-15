package com.minister.component.utils.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 网络运营商
 *
 * @author QIUCHANGQING620
 * @date 2021-03-17 11:22
 */
public enum NetIspEnum {
    /**
     * 中国移动
     */
    CMCC("CMCC", "中国移动"),
    /**
     * 中国电信
     */
    CTCC("CTCC", "中国电信"),
    /**
     * 中国联通
     */
    CUCC("CUCC", "中国联通"),
    /**
     * 其他
     */
    OTHER("OTHER", "其他"),
    ;

    private final String code;

    private final String desc;

    NetIspEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static NetIspEnum getNetIsp(String code) {
        return Arrays.stream(values()).filter(it -> it.getCode().equalsIgnoreCase(code)).findFirst().orElse(OTHER);
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}