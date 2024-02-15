package com.minister.component.utils.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.google.common.collect.Maps;
import com.minister.component.utils.JacksonUtil;
import com.minister.component.utils.entity.HeaderEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

/**
 * 获取 HeaderDto 工具
 *
 * @author QIUCHANGQING620
 * @date 2020-03-02 14:50
 */
public class HeadersContext {

    private static final TransmittableThreadLocal<HeaderEntity> ENTITY = new TransmittableThreadLocal<>();

    private static final TransmittableThreadLocal<Map<String, String>> CUSTOM = TransmittableThreadLocal.withInitial(Maps::newConcurrentMap);

    public static void clean() {
        CUSTOM.remove();
        ENTITY.remove();
    }

    public static void setHeaderEntity(HeaderEntity headerEntity) {
        ENTITY.set(headerEntity);
    }

    public static HeaderEntity getHeaderEntity() {
        HeaderEntity headerEntity = ENTITY.get();
        if (Objects.nonNull(headerEntity)) {
            return ENTITY.get();
        }
        headerEntity = new HeaderEntity();
        ENTITY.set(headerEntity);
        return headerEntity;
    }

    // ----- HeaderEntityBuilder start -----

    public static HeaderEntityBuilder builder() {
        return new HeaderEntityBuilder();
    }

    public static class HeaderEntityBuilder {

        private final Map<String, String> tmp = Maps.newHashMap();

        public HeaderEntityBuilder put(String headerName, String value) {
            if (StringUtils.isNotBlank(headerName)) {
                tmp.put(headerName, value);
            }
            return this;
        }

        public HeaderEntity build() {
            String jsonStr = JacksonUtil.bean2Json(tmp);
            return JacksonUtil.json2Bean(jsonStr, HeaderEntity.class);
        }

    }

    // ----- HeaderEntityBuilder end -----

    public static void setCustomHeader(String headerName, String value) {
        if (StringUtils.isNotBlank(headerName)) {
            Map<String, String> custom = CUSTOM.get();
            if (Objects.isNull(custom)) {
                custom = Maps.newConcurrentMap();
                CUSTOM.set(custom);
            }
            custom.put(headerName, value);
        }
    }

    public static String getCustomHeader(String headerName) {
        Map<String, String> custom = CUSTOM.get();
        if (Objects.isNull(custom)) {
            return null;
        }
        return custom.get(headerName);
    }

    public static void setCustomHeader(Map<String, String> customHeader) {
        CUSTOM.set(customHeader);
    }

    public static Map<String, String> getCustomHeader() {
        return CUSTOM.get();
    }

}
