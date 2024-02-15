package com.minister.component.elasticsearch.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Elastic配置项
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 17:32
 */
@Data
@Component
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticProperties {

    /**
     * ES地址
     */
    private String clusterName;

    /**
     * ES端口
     */
    private String clusterNodes;

    /**
     * ES用户名
     */
    private String username;

    /**
     * ES密码
     */
    private String password;

    /**
     * 超时时间
     */
    private Integer maxRetryTimeoutMillis;

    /**
     * 更新冲突时重试次数
     */
    private Integer retryOnConflict = 32;

    /**
     * ES keep alive 时间，分钟
     */
    private Long keepAliveStrategy = 30L;

}
