package com.minister.framework.debug.core.local.entity;

import lombok.Data;

import java.util.Map;

/**
 * 本地调试任务分发Entity
 *
 * @author QIUCHANGQING620
 * @date 2024-05-01 00:00
 */
@Data
public class DebugDispenseEntity {

    /**
     * 字段名称：微服务名称
     */
    private String serviceName;

    /**
     * 灰度项MAP
     * <debugType#debugValue, RuleItem>
     */
    Map<String, RuleItem> itemMap;

    /**
     * 配置项
     */
    @Data
    public static class RuleItem {

        /**
         * 调试类型 {@link com.minister.framework.debug.core.local.enums.DebugType}
         */
        private Integer debugType;

        /**
         * 灰度类型对应的值
         */
        private String debugValue;

        /**
         * 主机 ip:port，多个主机信息用英文逗号隔开
         */
        private String host;

        /**
         * 参数过滤条件，多个用应用逗号隔开
         */
        private String filter;

    }

}
