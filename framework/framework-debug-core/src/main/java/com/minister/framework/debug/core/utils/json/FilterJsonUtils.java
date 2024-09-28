package com.minister.framework.debug.core.utils.json;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FilterJsonUtils {

    private static ObjectMapper mapper = new ObjectMapper();
    private static ObjectMapper ignoreNullMapper = new ObjectMapper();
    static {
        recreateInnerObjectMapper(new HashMap<>());
        ignoreNullMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ignoreNullMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static void initObjectMapper(ObjectMapper objectMapper, Map<String, Object> jsonProperties) {

        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

//        registerTimeModule(objectMapper);

        Boolean ignoreNull = (Boolean)jsonProperties.get("ignoreNull");
        if(ignoreNull != null && ignoreNull) {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }

        Boolean writeLongAsString = (Boolean) jsonProperties.get("writeLongAsString");
        if (writeLongAsString != null && writeLongAsString) {
            SimpleModule module = new SimpleModule("Long-Module");
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            module.addSerializer(Long.class, ToStringSerializer.instance);
            objectMapper.registerModule(module);
        }

        Boolean writeNullAsEmpty = (Boolean) jsonProperties.get("writeNullAsEmpty");
        if (writeNullAsEmpty != null && writeNullAsEmpty) {
            objectMapper.setSerializerFactory(
                    objectMapper.getSerializerFactory()
                            .withSerializerModifier(new MyBeanSerializerModifier()));
        }

        Boolean writeDateAsString = (Boolean) jsonProperties.get("writeDateAsString");
        if (writeDateAsString != null && writeDateAsString) {
            objectMapper.setDateFormat(new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN));
        }

    }

    public static synchronized void recreateInnerObjectMapper(Map<String, Object> jsonProperties) {
        ObjectMapper objectMapper = new ObjectMapper();
        initObjectMapper(objectMapper, jsonProperties);
        mapper = objectMapper;
    }


    public static <T> T serializable(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static <T> T serializable(String json, TypeReference reference) {

        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return (T) mapper.readValue(json, reference);
        } catch (IOException e) {
            log.error(e.getMessage());
            return  null;
        }

    }

    public static String deserializer(Object json) {
        if (json == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return null;
        }

    }

    /**
     * 生成json的时候忽略对象中的null字段
     * @param object
     * @return
     */
    public static String ignoreNullObjectToJson(Object object) throws IOException {
        if (object == null) {
            return null;
        }
        return ignoreNullMapper.writeValueAsString(object);
    }


    public static ObjectMapper getMapper() {
        return mapper;
    }

}
