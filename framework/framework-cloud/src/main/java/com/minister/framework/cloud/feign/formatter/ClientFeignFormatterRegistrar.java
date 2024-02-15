package com.minister.framework.cloud.feign.formatter;

import cn.hutool.core.date.DatePattern;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 客户端 feign 格式化设置
 * 用于修复 feign 调用存在8小时时差
 *
 * @author QIUCHANGQING620
 * @date 2020-03-06 23:45
 */
@Component
public class ClientFeignFormatterRegistrar implements FeignFormatterRegistrar {

    public ClientFeignFormatterRegistrar() {
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(Date.class, String.class, new DateToStringConverter());
    }

    private static class DateToStringConverter implements Converter<Date, String> {
        @Override
        public String convert(@NonNull Date source) {
            return DateFormatUtils.format(source, DatePattern.NORM_DATETIME_PATTERN);
        }
    }

}