package com.minister.framework.debug.core.stg.config;

import com.minister.component.utils.constants.ApplicationConstant;
import com.minister.framework.debug.core.stg.cache.MultiVersionCache;
import com.minister.framework.debug.core.stg.entity.MultiVersionEntity;
import com.minister.framework.debug.core.stg.utils.MultiVersionConfigHttpCaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 多版本测试规则获取定时任务
 *
 * @author QIUCHANGQING620
 * @date 2024-05-02 00:00
 */
@Component
@Slf4j
public class MultiVersionScheduler {

    @Resource
    private ApplicationConstant applicationConstant;

    /**
     * 轮询更新多版本测试规则
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    public void obtainDebugConfig() {
        // 服务名称
        String applicationName = applicationConstant.getApplicationName().toUpperCase();
        String env = applicationConstant.getEnv().toUpperCase();
        // 设置了开关配置为true的才进入调试模式
        if (applicationConstant.isServiceDebugGate()) {
            try {
                List<MultiVersionEntity> list = MultiVersionConfigHttpCaller.getMultiVersionInfo(applicationName, env);
                MultiVersionCache.refreshMultiVersionCache(list);
            } catch (Exception e) {
                log.warn("get multi version config fail");
            }
        }
    }

}