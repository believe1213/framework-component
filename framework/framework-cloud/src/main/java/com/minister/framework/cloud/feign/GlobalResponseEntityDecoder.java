package com.minister.framework.cloud.feign;

import com.google.inject.util.Types;
import com.minister.component.utils.enums.HttpCodeEnum;
import com.minister.framework.api.entity.ResponseDto;
import com.minister.framework.boot.exception.FeignException;
import com.minister.framework.cloud.feign.properties.FeignProperties;
import feign.Response;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * GlobalResponseEntityDecoder
 * 参考 ResponseEntityDecoder 实现
 *
 * @author QIUCHANGQING620
 * @date 2020-03-06 22:18
 */
@Slf4j
public class GlobalResponseEntityDecoder implements Decoder {

    /**
     * 映射 : 基本类型 -> 对象类型
     */
    private static final Map<String, Type> PRIMITIVE_TYPE_MAP;

    static {
        Map<String, Type> tmpPrimitiveTypeMap = new TreeMap<>();
        tmpPrimitiveTypeMap.put("boolean", Boolean.class);
        tmpPrimitiveTypeMap.put("char", Character.class);
        tmpPrimitiveTypeMap.put("byte", Byte.class);
        tmpPrimitiveTypeMap.put("short", Short.class);
        tmpPrimitiveTypeMap.put("int", Integer.class);
        tmpPrimitiveTypeMap.put("long", Long.class);
        tmpPrimitiveTypeMap.put("float", Float.class);
        tmpPrimitiveTypeMap.put("double", Double.class);

        PRIMITIVE_TYPE_MAP = Collections.unmodifiableMap(tmpPrimitiveTypeMap);
    }

    private final Decoder decoder;

    public GlobalResponseEntityDecoder(Decoder decoder) {
        this.decoder = decoder;
    }

    /**
     * 解码
     *
     * @param response response
     * @param type     解码目标对象
     */
    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        if (isInWhiteList(response.request().url())) {
            return this.decoder.decode(response, type);
        }
        // ParameterizedType就是参数化类型，声明类型中带有“<>”的都是参数化类型
        // getActualTypeArguments()返回Type[]，即“<>”里的参数，比如Map<String,Integer>获取String,Integer
        // getRawType()返回Tpye，得到“<>”前面的类型，比如List<String>获取String
        if (isParameterizeHttpEntity(type)) {
            // 判断 type 是否为 HttpEntity 的参数化类型
            type = ((ParameterizedType) type).getActualTypeArguments()[0];
            Object decodedObject = this.decoder.decode(response, type);

            return createResponse(decodedObject, response);
        } else if (isHttpEntity(type)) {
            // 判断 type 类型是否为 HttpEntity 类型
            return createResponse(null, response);
        } else if (isParameterizeResponseDto(type) || isResponseDto(type)) {
            // 判断 type 是否为 ResponseDto 的参数化类型，或者 ResponseDto 类型
            Object decodedObject = this.decoder.decode(response, type);

            return isSuccess(decodedObject);
        } else {
            // 如果目标对象非 HttpEntity 和 ResponseDto
            // 1. 如果type为基本类型，则转换为包装类型，例：int->Integer
            Type newType = PRIMITIVE_TYPE_MAP.get(type.getTypeName());
            // 2. 把 type 封装成 ResponseDto<?>
            type = Types.newParameterizedType(ResponseDto.class, newType == null ? type : newType);
            // 3. 解码
            Object decodedObject = this.decoder.decode(response, type);
            // 4. 判断状态值
            ResponseDto<?> responseDto = isSuccess(decodedObject);
            // 5. 拆包(去除 code & msg)
            return responseDto.getData();
        }
    }

    /**
     * 路由判断
     */
    private boolean isInWhiteList(String url) {
        Set<String> whiteList = FeignProperties.decodeWhiteListUri;

        for (String s : whiteList) {
            if (url.contains(s)) {
                return true;
            }
        }

        return false;
    }

    private boolean isParameterizeHttpEntity(Type type) {
        if (type instanceof ParameterizedType) {
            return isHttpEntity(((ParameterizedType) type).getRawType());
        }
        return false;
    }

    private boolean isHttpEntity(Type type) {
        if (type instanceof Class) {
            Class<?> c = (Class<?>) type;
            return HttpEntity.class.isAssignableFrom(c);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private <T> ResponseEntity<T> createResponse(Object instance, Response response) {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        for (String key : response.headers().keySet()) {
            headers.put(key, new LinkedList<>(response.headers().get(key)));
        }

        return new ResponseEntity<>((T) instance, headers, HttpStatus.valueOf(response.status()));
    }

    private boolean isParameterizeResponseDto(Type type) {
        if (type instanceof ParameterizedType) {
            return isResponseDto(((ParameterizedType) type).getRawType());
        }
        return false;
    }

    private boolean isResponseDto(Type type) {
        if (type instanceof Class) {
            Class<?> c = (Class<?>) type;
            return ResponseDto.class.isAssignableFrom(c);
        }
        return false;
    }

    private ResponseDto<?> isSuccess(Object decodedObject) {
        if (decodedObject == null) {
            throw new FeignException("Feign decoded object is null.");
        }
        ResponseDto<?> responseDto = (ResponseDto<?>) decodedObject;
        if (!HttpCodeEnum.OK.getCode().equals(responseDto.getCode())) {
            throw new FeignException(responseDto.getCode(), responseDto.getMsg(), responseDto.getTraceId());
        }

        return responseDto;
    }

}
