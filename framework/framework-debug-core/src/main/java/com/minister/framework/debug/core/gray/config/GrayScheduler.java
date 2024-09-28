package com.minister.framework.debug.core.gray.config;

import com.minister.component.utils.constants.ApplicationConstant;
import com.minister.framework.debug.core.gray.entity.GrayTaskDispenseEntity;
import com.minister.framework.debug.core.gray.helper.GrayForwardHelper;
import com.minister.framework.debug.core.gray.utils.GrayConfigHttpCaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

/**
 * 灰度规则获取定时任务
 *
 * @author QIUCHANGQING620
 * @date 2024-05-03 00:00
 */
@Component
@Slf4j
public class GrayScheduler {

    @Resource
    private ApplicationConstant applicationConstant;

    /**
     * 轮询更新调试规则: 次/5秒查询
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void obtainDebugConfig() {
        // 服务名称
        String applicationName = applicationConstant.getApplicationName().toUpperCase();
        String env = applicationConstant.getEnv().toUpperCase();
        if (applicationConstant.isServiceGrayGate()) {
            try {
                GrayTaskDispenseEntity grayTaskDispenseEntity = GrayConfigHttpCaller.getRuleByServiceName(applicationName, env);
                if (Objects.nonNull(grayTaskDispenseEntity)) {
                    Map<String, GrayTaskDispenseEntity.RuleItem> map = grayTaskDispenseEntity.getItemMap();
                    GrayForwardHelper.refreshGrayRuleCache(map);
                }
            } catch (Exception e) {
                log.warn("get gray config fail");
            }
        }
    }

}