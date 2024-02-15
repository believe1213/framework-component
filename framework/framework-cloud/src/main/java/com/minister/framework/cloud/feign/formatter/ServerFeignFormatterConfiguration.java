package com.minister.framework.cloud.feign.formatter;

import cn.hutool.core.date.DatePattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;

/**
 * 服务器 feign 格式化设置
 * 用于修复 feign 调用存在8小时时差
 *
 * @author QIUCHANGQING620
 * @date 2020-03-06 23:54
 */
@Configuration
@Slf4j
public class ServerFeignFormatterConfiguration {

    @Resource
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    /**
     * 增加字符串转日期的功能
     */
    @PostConstruct
    public void initEditableValidation() {
        ConfigurableWebBindingInitializer initializer = (ConfigurableWebBindingInitializer) requestMappingHandlerAdapter.getWebBindingInitializer();
        if (initializer != null && initializer.getConversionService() != null) {
            GenericConversionService genericConversionService = (GenericConversionService) initializer.getConversionService();
            genericConversionService.addConverter(String.class, Date.class, new StringToDateConverter());
        }
    }

    private static class StringToDateConverter implements Converter<String, Date> {
        @Override
        public Date convert(@NonNull String source) {
            try {
                return DateUtils.parseDate(source, DatePattern.NORM_DATETIME_PATTERN);
            } catch (ParseException e) {
                log.error("Fail to converter string to Date in feign server.");
                return null;
            }
        }
    }

}
