package com.minister.framework.debug.core.stg.cache;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.minister.framework.debug.core.stg.entity.MultiVersionEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 多版本测试配置缓存
 *
 * @author QIUCHANGQING620
 * @date 2024-05-02 00:00
 */
public class MultiVersionCache {

    private static volatile List<MultiVersionEntity> multiVersionList = new ArrayList<>();

    /**
     * 更新调试跳转配置到缓存
     */
    public static void refreshMultiVersionCache(List<MultiVersionEntity> list) {
        if (CollUtil.isEmpty(list)) {
            multiVersionList = new ArrayList<>();
        } else {
            multiVersionList = new ArrayList<>(list);
        }
    }

    /**
     * 获取所有配置
     */
    public static List<MultiVersionEntity> getMultiVersionCache() {
        return multiVersionList;
    }

    /**
     * 根据环境标识获取多版本配置信息
     */
    public static MultiVersionEntity getMultiVersion(String envId) {
        if (StringUtils.isBlank(envId)) {
            return null;
        }
        for (MultiVersionEntity m : multiVersionList) {
            if (StrUtil.equals(m.getEnvId(), envId)) {
                return m;
            }
        }
        return null;
    }

    public static boolean isCacheEmpty() {
        return CollUtil.isEmpty(multiVersionList);
    }

}
