package com.minister.framework.boot.config;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Logback 配置项刷新
 * 仅支持如下配置项：
 * log.file.level
 * log.file.sql.level
 * log.file.http.level
 *
 * @author QIUCHANGQING620
 * @date 2022-10-28 13:46
 */
@Component
@Slf4j
public class LogbackRefresher implements EnvironmentAware {

    private ConfigurableEnvironment environment;

    @Resource
    private LoggingSystem loggingSystem;

    @Value("${log.file.level:info}")
    public void setLogLevel(String logLevel) {
        onChangeLog();
    }

    @Value("${log.file.sql.level:info}")
    public void setLogSqlLevel(String logSqlLevel) {
        onChangeLog();
    }

    @Value("${log.file.http.level:info}")
    public void setLogHttpLevel(String logHttpLevel) {
        onChangeLog();
    }

    private void onChangeLog() {
        if (environment == null) {
            // 依赖注入阶段 environment 还未初始化
            return;
        }
        String logConfig = environment.getProperty("logging.config");
        if (StringUtils.isBlank(logConfig)) {
            log.error("can not find logging.config");
            return;
        }
        try {
            ResourceUtil.getStream(logConfig).close();
            loggingSystem.cleanUp();
            loggingSystem.initialize(new LoggingInitializationContext(environment), logConfig, null);
        } catch (Exception e) {
            log.error("clean & init loggingSystem fail", e);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

}
