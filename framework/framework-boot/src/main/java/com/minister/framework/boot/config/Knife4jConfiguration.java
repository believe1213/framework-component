package com.minister.framework.boot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * Knife4j 配置类
 *
 * @author QIUCHANGQING620
 * @date 2020-03-07 23:41
 */
@Configuration
@EnableSwagger2WebMvc
@Import(BeanValidatorPluginsConfiguration.class)
public class Knife4jConfiguration {

    private static final String SUFFIX = " Api Documentation";

    @Value("${spring.application.name:application}")
    private String application;

    @Value("${framework-boot.knife4j.description:}")
    private String description;

    @Value("${framework-boot.knife4j.version:1.0.0}")
    private String version;

    @Value("${framework-boot.knife4j.serviceUrl:}")
    private String serviceUrl;

    @Value("${framework-boot.knife4j.package:com.pingan.smartcity}")
    private String basePackage;

    @Bean
    public Docket knife4jDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 指定Controller扫描包路径
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(application.toUpperCase() + SUFFIX)
                .description(description)
                .version(version)
                .termsOfServiceUrl(serviceUrl)
                .build();
    }

}
