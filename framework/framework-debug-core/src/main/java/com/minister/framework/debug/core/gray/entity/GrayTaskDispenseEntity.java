package com.minister.framework.debug.core.gray.entity;

import lombok.Data;

import java.util.Map;

/**
 * 灰度任务分发Entity
 *
 * @author QIUCHANGQING620
 * @date 2024-05-03 00:00
 */
@Data
public class GrayTaskDispenseEntity {

    /**
     * 字段名称：微服务名称
     */
    private String serviceName;

    /**
     * 灰度项MAP
     * <grayType#grayValue, RuleItem>
     */
    Map<String, RuleItem> itemMap;

    /**
     * 配置项
     */
    @Data
    public static class RuleItem {

        /**
         * 灰度主题ID
         */
        private Long taskId;

        /**
         * 灰度类型 {@link com.minister.framework.debug.core.gray.enums.GrayType}
         */
        private Integer grayType;

        /**
         * 灰度类型对应的值
         */
        private String grayValue;

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
