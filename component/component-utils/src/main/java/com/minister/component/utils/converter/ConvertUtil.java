package com.minister.component.utils.converter;

import com.minister.component.utils.entity.PageVo;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 转换器工具类
 *
 * @author QIUCHANGQING620
 * @date 2020-02-19 19:51
 */
public class ConvertUtil {

    @SuppressWarnings("unchecked")
    public static <S, T> Collection<T> convertCol(Collection<S> source, Class<S> sClass, Class<T> tClass, ConversionService conversionService) {
        if (source == null) {
            return new ArrayList<>();
        }
        return (Collection<T>) conversionService.convert(
                source,
                TypeDescriptor.collection(source.getClass(), TypeDescriptor.valueOf(sClass)),
                TypeDescriptor.collection(source.getClass(), TypeDescriptor.valueOf(tClass))
        );
    }

    public static <S, T> T convertObj(S source, Class<T> tClass, ConversionService conversionService) {
        if (source == null) {
            return null;
        }
        return conversionService.convert(source, tClass);
    }

    public static <S, T> PageVo<T> convertPageVo(PageVo<S> source, Class<S> sClass, Class<T> tClass, ConversionService conversionService) {
        if (source == null) {
            return new PageVo<>();
        }

        return new PageVo<>(
                source.getCurrentPage(),
                source.getPageSize(),
                source.getTotal(),
                convertCol(source.getRecords(), sClass, tClass, conversionService)
        );
    }

    // TODO 待完善
    public static boolean canConvertElements(TypeDescriptor sourceElementType, TypeDescriptor targetElementType, ConversionService conversionService) {
        if (targetElementType == null) {
            return true;
        }
        if (sourceElementType == null) {
            return true;
        }
        if (conversionService.canConvert(sourceElementType, targetElementType)) {
            return true;
        }
        if (sourceElementType.getType().isAssignableFrom(targetElementType.getType())) {
            return true;
        }
        return false;
    }

}
