package com.minister.framework.debug.core.utils.json;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 处理null值序列化
 */
public class MyBeanSerializerModifier extends BeanSerializerModifier {

    private JsonSerializer nullStringJsonSerializer = new NullStringSerializer();

    private JsonSerializer nullListJsonSerializer = new NullListSerializer();

    private JsonSerializer nullMapJsonSerializer = new NullMapSerializer();


    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {

        for (BeanPropertyWriter writer : beanProperties) {
            if (isStringType(writer)) {
                writer.assignNullSerializer(nullStringJsonSerializer);
            }

            if (isListType(writer)) {
                writer.assignNullSerializer(nullListJsonSerializer);
            }

            if (isMapType(writer)) {
                writer.assignNullSerializer(nullMapJsonSerializer);
            }

        }

        return beanProperties;

    }

    //判断是什么类型
    protected boolean isStringType(BeanPropertyWriter writer) {
        Class clazz = writer.getType().getRawClass();
        return clazz.equals(String.class);
    }

    protected boolean isListType(BeanPropertyWriter writer) {
        Class clazz = writer.getType().getRawClass();
        //ArrayList,LinkedList ?
        return clazz.isArray() || clazz.equals(List.class) || clazz.equals(Set.class);
    }

    protected boolean isMapType(BeanPropertyWriter writer) {
        Class clazz = writer.getType().getRawClass();
        //HashMap,TreeMap ?
        return clazz.equals(Map.class);
    }

}
