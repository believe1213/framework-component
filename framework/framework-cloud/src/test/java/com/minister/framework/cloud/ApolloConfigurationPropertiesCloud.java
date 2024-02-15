package com.minister.framework.cloud;

import cn.hutool.extra.spring.SpringUtil;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.stereotype.Component;

/**
 * 支持apollo热配置的ConfigurationProperties
 * cloud写法
 *
 * @author QIUCHANGQING620
 * @date 2020-11-26 18:17
 */
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "framework-cloud")
@Data
public class ApolloConfigurationPropertiesCloud {

    private String test;

    @ApolloConfigChangeListener(interestedKeyPrefixes = "framework-cloud")
    public void configChange(ConfigChangeEvent configChangeEvent) {
        SpringUtil.getApplicationContext().publishEvent(new EnvironmentChangeEvent(configChangeEvent.changedKeys()));
    }

}
