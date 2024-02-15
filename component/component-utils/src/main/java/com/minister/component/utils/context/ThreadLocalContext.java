package com.minister.component.utils.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 本地线程Util
 *
 * @author QIUCHANGQING620
 * @date 2020-04-25 19:51
 */
@Slf4j
public class ThreadLocalContext {

    private ThreadLocalContext() {
    }

    public static TransmittableThreadLocal<Map<String, Object>> THREAD_LOCAL = TransmittableThreadLocal.withInitial(Maps::newConcurrentMap);

    public static boolean containsKey(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        Map<String, Object> map = THREAD_LOCAL.get();
        return map.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        Map<String, Object> map = THREAD_LOCAL.get();
        if (MapUtils.isEmpty(map)) {
            return null;
        }

        try {
            return (T) map.get(key);
        } catch (Exception e) {
            log.error("parse error", e);
            return null;
        }
    }

    public static void put(String key, Object value) {
        if (StringUtils.isBlank(key) || value == null) {
            return;
        }
        Map<String, Object> map = THREAD_LOCAL.get();
        map.put(key, value);
    }

    public static void putAll(Map<String, Object> m) {
        if (MapUtils.isEmpty(m)) {
            return;
        }
        Map<String, Object> map = THREAD_LOCAL.get();
        map.putAll(m);
    }

    public static void remove(String key) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        Map<String, Object> map = THREAD_LOCAL.get();
        map.remove(key);
    }

    public static void clear() {
        THREAD_LOCAL.get().clear();
    }

    public static void clean() {
        THREAD_LOCAL.remove();
    }

}
