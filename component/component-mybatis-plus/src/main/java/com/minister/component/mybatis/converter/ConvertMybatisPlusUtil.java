package com.minister.component.mybatis.converter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.minister.component.utils.converter.ConvertUtil;
import com.minister.component.utils.entity.PageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;

/**
 * 转换器工具类
 *
 * @author QIUCHANGQING620
 * @date 2020-02-19 20:08
 */
@Slf4j
public class ConvertMybatisPlusUtil extends ConvertUtil {

    public static <S, T> PageVo<T> convertVo(IPage<S> source, Class<S> sClass, Class<T> tClass, ConversionService conversionService) {
        if (source == null) {
            return new PageVo<>();
        }
        if (source.getRecords() == null) {
            return new PageVo<>((int) source.getCurrent(), (int) source.getSize(), (int) source.getTotal());
        }

        if (source.searchCount()) {
            return new PageVo<>(
                    (int) source.getCurrent(),
                    (int) source.getSize(),
                    (int) source.getTotal(),
                    convertCol(source.getRecords(), sClass, tClass, conversionService)
            );
        } else {
            return new PageVo<>(
                    (int) source.getCurrent(),
                    (int) source.getSize(),
                    convertCol(source.getRecords(), sClass, tClass, conversionService)
            );
        }
    }

}
