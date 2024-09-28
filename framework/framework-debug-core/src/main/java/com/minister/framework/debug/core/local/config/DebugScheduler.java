package com.minister.framework.debug.core.local.config;

import com.minister.component.utils.constants.ApplicationConstant;
import com.minister.framework.debug.core.local.entity.DebugDispenseEntity;
import com.minister.framework.debug.core.local.helper.DebugForwardHelper;
import com.minister.framework.debug.core.local.utils.DebugConfigHttpCaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

/**
 * 本地调试规则获取定时任务
 *
 * @author QIUCHANGQING620
 * @date 2024-05-01 00:00
 */
@Component
@Slf4j
public class DebugScheduler {

    @Resource
    private ApplicationConstant applicationConstant;

    /**
     * 轮询更新本地调试规则
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    public void obtainDebugConfig() {
        // 服务名称
        String applicationName = applicationConstant.getApplicationName().toUpperCase();
        String env = applicationConstant.getEnv().toUpperCase();
        // 设置了开关配置为true的才进入调试模式
        if (applicationConstant.isServiceDebugGate()) {
            try {
                DebugDispenseEntity debugDispenseEntity = DebugConfigHttpCaller.getForwardInfo(applicationName, env);
                if (Objects.nonNull(debugDispenseEntity)) {
                    Map<String, DebugDispenseEntity.RuleItem> map = debugDispenseEntity.getItemMap();
                    DebugForwardHelper.refreshForwardCache(map);
                }
            } catch (Exception e) {
                log.warn("get online debug config fail");
            }
            try {
                Map<String, String> natRsp = DebugConfigHttpCaller.allNatAddress(env);
                DebugForwardHelper.refreshHostIPRelationCache(natRsp);
            } catch (Exception e) {
                log.warn("get online debug host relation fail");
            }
        }
    }

}
