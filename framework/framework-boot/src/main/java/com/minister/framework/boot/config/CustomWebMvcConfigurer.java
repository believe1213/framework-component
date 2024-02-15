package com.minister.framework.boot.config;

import com.minister.component.trace.interceptor.TraceHandlerInterceptor;
import com.minister.framework.boot.header.interceptor.HeaderHandlerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web相关 框架级别拦截器
 *
 * @author QIUCHANGQING620
 * @date 2020-03-07 00:23
 */
@Configuration
@Slf4j
public class CustomWebMvcConfigurer implements WebMvcConfigurer {

    @Value("${framework-boot.file-path-window:C:\\fileResources\\}")
    private String filePathWindow;

    @Value("${framework-boot.file-path-linux:/fileResources/}")
    private String filePathLinux;

    /**
     * 不设置时 response data 为 String 时, ResponseBodyAdvice 中的 MediaType 会采用 TEXT_PLAIN
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * 配置静态资源读取目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String os = System.getProperty("os.name");

        if (os.toLowerCase().startsWith("win")) {
            // 如果是 Windows 系统
            // addResourceHandler = uri 请求路劲
            // addResourceLocations = 服务器真实路劲
            // 把 filePathWindow 目录下的所有资源解析到 /static_file/** 路径
            registry.addResourceHandler("/static_file/**")
                    .addResourceLocations("file:" + filePathWindow);
        } else {
            // 如果是 linux 或 mac 系统
            registry.addResourceHandler("/static_file/**")
                    .addResourceLocations("file:" + filePathLinux);
        }

        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HeaderHandlerInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(new TraceHandlerInterceptor()).addPathPatterns("/**");
    }

}
