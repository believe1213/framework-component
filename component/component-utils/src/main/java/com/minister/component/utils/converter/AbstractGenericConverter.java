package com.minister.component.utils.converter;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.converter.GenericConverter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Set;

/**
 * 转换器注册抽象类
 *
 * @author QIUCHANGQING620
 * @date 2020-02-19 19:45
 */
public abstract class AbstractGenericConverter implements GenericConverter {

    @Resource(name = "mvcConversionService")
    protected ConversionService conversionService;

    @Override
    public abstract Set<ConvertiblePair> getConvertibleTypes();

    @PostConstruct
    private void register() {
        if (conversionService instanceof ConverterRegistry) {
            ((ConverterRegistry) conversionService).addConverter(this);
        } else {
            throw new IllegalStateException(String.format("[%s] register fail", this.getClass().getName()));
        }
    }

}
