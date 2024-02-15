package com.minister.component.utils;

import cn.hutool.core.date.DateException;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Function;

/**
 * JacksonUtil
 *
 * @author QIUCHANGQING620
 * @date 2020-06-15 23:13
 */
@Slf4j
@Component
public class JacksonUtil implements InitializingBean {

    private JacksonUtil() {
    }

    @Value("${component-utils.production:false}")
    private boolean production;

    private static final ObjectMapper OM = new ObjectMapper();

    static {
        // 是否允许解析使用Java/C++ 样式的注释（包括'/*' 和'//'）。
        OM.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // 是否允许解析使用YAML样式的注释（包括'#'）
        OM.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        // 是否将允许使用非双引号属性名字
//        om.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 是否允许单引号来包住属性名称和字符串值
//        om.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 是否按字母顺序排序属性
        OM.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        // 不可序列化类是否抛出异常
        OM.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 序列化枚举是否以toString()来输出，如果没有toString()方法则采用name()输出，默认值为false以name()来输出
        OM.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        // json转换为对象时，含有未知的属性是否抛出异常
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // json转换为对象时，数据类型为primitive（例如int和double）时，如果值为null是否抛出异常
        OM.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        // 和SerializationFeature.WRITE_ENUMS_USING_TO_STRING共同使用
        OM.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        // 设置时间序列化
        OM.setDateFormat(new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN));
        // 设置时区
        OM.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        // jdk8支持
        OM.registerModule(new JavaTimeModule());
        OM.registerModule(new Jdk8Module());
        OM.registerModule(new ParameterNamesModule());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!production) {
            // 美化输出
            OM.configure(SerializationFeature.INDENT_OUTPUT, true);
        }
    }

    /**
     * 对象转字符串
     *
     * @param bean 对象
     * @param <T>  对象类型
     * @return 字符串
     */
    public static <T> String bean2Json(T bean) {
        if (Objects.isNull(bean)) {
            return null;
        }
        try {
            return OM.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            log.error("bean2Json->fail", e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 字符串转对象
     *
     * @param json 字符串
     * @param type 对象类
     * @param <T>  对象类型
     * @return 对象
     */
    public static <T> T json2Bean(String json, Class<T> type) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return OM.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("json2Bean->fail", e);
        }
        return null;
    }

    /**
     * 字符串转对象
     *
     * @param json    字符串
     * @param typeRef 类引用
     * @param <T>     对象类型
     * @return 对象
     */
    public static <T> T json2Bean(String json, TypeReference<T> typeRef) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return OM.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("json2Bean->fail", e);
        }
        return null;
    }

    /**
     * 字符串转对象
     *
     * @param json     字符串
     * @param javaType javaType
     * @param <T>      对象类型
     * @return 对象
     */
    public static <T> T json2Bean(String json, JavaType javaType) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return OM.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            log.error("json2Bean->fail", e);
        }
        return null;
    }

    /**
     * 对象转换
     *
     * @param from   原对象
     * @param toType 对象类
     * @param <T>    对象类型
     * @return 对象
     */
    public static <T> T convertValue(Object from, Class<T> toType) {
        if (Objects.isNull(from)) {
            return null;
        }
        try {
            return OM.convertValue(from, toType);
        } catch (IllegalArgumentException e) {
            log.error("convertValue->fail", e);
        }
        return null;
    }

    /**
     * 对象转换
     *
     * @param from    原对象
     * @param typeRef 类引用
     * @param <T>     对象类型
     * @return 对象
     */
    public static <T> T convertValue(Object from, TypeReference<T> typeRef) {
        if (Objects.isNull(from)) {
            return null;
        }
        try {
            return OM.convertValue(from, typeRef);
        } catch (IllegalArgumentException e) {
            log.error("convertValue->fail", e);
        }
        return null;
    }

    /**
     * 判断字符串是否为json(null也会被判定为true)
     *
     * @param str 字符串
     * @return 是否为json
     */
    public static boolean isJson(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        try {
            OM.readTree(str);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    public static JsonNode readTree(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        try {
            return OM.readTree(str);
        } catch (JsonProcessingException e) {
            log.error("readTree->fail", e);
        }
        return null;
    }

    /**
     * 获取类型工厂（可用于生成JavaType）
     *
     * @return 类型工厂
     */
    public static TypeFactory typeFactory() {
        return OM.getTypeFactory();
    }

    /**
     * 复制ObjectMapper
     *
     * @return ObjectMapper
     */
    public static ObjectMapper copy() {
        return OM.copy();
    }

    /**
     * 递归遍历JsonNode并处理String类型数据
     *
     * @param node     JsonNode
     * @param function String处理方法
     * @return JsonNode
     */
    public static JsonNode traversal(JsonNode node, Function<String, String> function) {
        if (node.isTextual()) {
            // String
            String str = node.asText();
            try {
                DateUtil.parse(str);
            } catch (DateException e) {
                return convertValue(function.apply(str), JsonNode.class);
            }
        } else if (node.isObject()) {
            // Object
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                entry.setValue(traversal(entry.getValue(), function));
            }
        } else if (node.isArray()) {
            // collection
            for (JsonNode jsonNode : node) {
                traversal(jsonNode, function);
            }
        }
        // null and other do nothing

        return node;
    }

}
