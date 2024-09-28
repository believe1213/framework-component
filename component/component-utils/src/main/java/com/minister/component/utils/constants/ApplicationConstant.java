package com.minister.component.utils.constants;

import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * 框架变量配置
 *
 * @author QIUCHANGQING620
 * @date 2024-04-28 00:30
 */
@Data
public class ApplicationConstant {

    /**
     * 设置json序列化的时候是否忽略对象中的null属性, 默认为不忽略
     */
    @Value("${kye.framework.json.ignoreNull:false}")
    private boolean jsonIgnoreNull;

    /**
     * 设置json序列化的时候是否将Long类型转成String类型，避免前端溢出造成精度丢失
     */
    @Value("${kye.framework.json.writeLongAsString:false}")
    private boolean writeLongAsString;

    /**
     * 设置json序列化的时候是否将Date类型转成String(yyyy-MM-dd HH:mm:ss)类型，避免前端溢出造成精度丢失
     */
    @Value("${kye.framework.json.writeDateAsString:false}")
    private boolean writeDateAsString;

    /**
     * 设置json序列化的时候是否将null String,List,Map 等类型转成空对象.
     */
    @Value("${kye.framework.json.writeNullAsEmpty:false}")
    private boolean writeNullAsEmpty;

    /**
     * 应用名称
     */
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 服务端口
     */
    @Value("${server.port:8080}")
    private String port;

    /**
     * 框架环境标识
     */
    @Value("${kye.framework.env:}")
    private String frameworkEnv;

    /**
     * 调试平台开关，默认是关闭
     */
    @Value("${service.debug.gate:false}")
    private boolean serviceDebugGate;

    /**
     * 啄木鸟灰度开关，默认是开启
     */
    @Value("${service.gray.gate:true}")
    private boolean serviceGrayGate;

    public Map<String, Object> getJsonProperties() {
        Map<String, Object> jsonProperties = Maps.newHashMap();
        jsonProperties.put("ignoreNull", jsonIgnoreNull);
        jsonProperties.put("writeLongAsString", writeLongAsString);
        jsonProperties.put("writeDateAsString", writeDateAsString);
        jsonProperties.put("writeNullAsEmpty", writeNullAsEmpty);
        return jsonProperties;
    }

    /**
     * 获取环境标识
     */
    public String getEnv() {
        if (StringUtils.isNotBlank(frameworkEnv)) {
            // 优先1：使用框架提供的环境标识参数
            return frameworkEnv;
        }
        return null;
    }

}
