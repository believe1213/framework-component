package com.minister.framework.debug.core.config;

import com.minister.framework.debug.core.gray.GrayFilter;
import com.minister.framework.debug.core.local.DebugFilter;
import com.minister.framework.debug.core.stg.MultiVersionFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web相关 框架级别拦截器
 *
 * @author QIUCHANGQING620
 * @date 2024-05-01 00:00
 */
@Configuration
@Slf4j
public class DebugWebMvcConfigurer implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean debugFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new DebugFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 101);
        return registration;
    }

    @Bean
    public FilterRegistrationBean multiVersionFilterRegistration() {
        //是否初始化
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new MultiVersionFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 102);
        return registration;
    }

    @Bean
    public FilterRegistrationBean grayFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new GrayFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 103);
        return registration;
    }

}
