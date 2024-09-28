package com.minister.component.utils.context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.minister.component.utils.JacksonUtil;
import com.minister.component.utils.StrUtil;
import com.minister.component.utils.entity.HeaderEntity;
import com.minister.component.utils.function.Tuple2;
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

    private static final ThreadLocal<HeaderEntity> ENTITY = ThreadLocal.withInitial(HeaderEntity::new);

    private static final ThreadLocal<Map<String, String>> CUSTOM = ThreadLocal.withInitial(Maps::newConcurrentMap);

    public static HeaderEntity getHeaderEntity() {
        HeaderEntity headerEntity = ENTITY.get();
        if (Objects.nonNull(headerEntity)) {
            return headerEntity;
        }
        headerEntity = new HeaderEntity();
        ENTITY.set(headerEntity);
        return headerEntity;
    }

    public static Map<String, String> getCustomHeader() {
        Map<String, String> customHeader = CUSTOM.get();
        if (Objects.nonNull(customHeader)) {
            return customHeader;
        }
        customHeader = Maps.newConcurrentMap();
        CUSTOM.set(customHeader);
        return customHeader;
    }

    public static String getCustomHeader(String headerName) {
        Map<String, String> custom = CUSTOM.get();
        if (Objects.isNull(custom)) {
            return null;
        }
        return custom.get(headerName);
    }

    public static String getCustomHeaderIgnoreCase(String headerName) {
        Map<String, String> custom = CUSTOM.get();
        if (Objects.isNull(custom)) {
            return null;
        }
        for (Map.Entry<String, String> entry : custom.entrySet()) {
            if (StrUtil.equalsIgnoreCase(entry.getKey(), headerName)) {
                return entry.getValue();
            }
        }

        return null;
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

    public static void clean() {
        CUSTOM.remove();
        ENTITY.remove();
    }

    public static Tuple2<HeaderEntity, Map<String, String>> getAll() {
        HeaderEntity headerEntity = ENTITY.get();
        Map<String, String> customHeaders = CUSTOM.get();
        return new Tuple2<>(headerEntity, customHeaders);
    }

    public static void setAll(HeaderEntity headerEntity, Map<String, String> customHeaders) {
        ENTITY.set(headerEntity);
        CUSTOM.set(customHeaders);
    }

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

    public static Tuple2<HeaderEntity, Map<String, String>> copyAll() {
        HeaderEntity headerEntity = JacksonUtil.convertValue(ENTITY.get(), HeaderEntity.class);
        Map<String, String> customHeaders = JacksonUtil.convertValue(CUSTOM.get(), new TypeReference<Map<String, String>>() {
        });
        return new Tuple2<>(headerEntity, customHeaders);
    }

    public static void setHeaderEntity(HeaderEntity headerEntity) {
        ENTITY.set(headerEntity);
    }

    public static void setCustomHeader(Map<String, String> customHeader) {
        CUSTOM.set(customHeader);
    }

}
