package com.minister.framework.debug.core.stg.entity;

import lombok.Data;

/**
 * 多版本测试任务分发Entity
 *
 * @author QIUCHANGQING620
 * @date 2024-05-02 00:00
 */
@Data
public class MultiVersionEntity {

    /**
     * 环境ID
     */
    private String envId;

    /**
     * 跳转主机信息（IP:PORT）
     */
    private String host;

}
