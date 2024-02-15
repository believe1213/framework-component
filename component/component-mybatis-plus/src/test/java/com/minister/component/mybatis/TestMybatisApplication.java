package com.minister.component.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TestMybatisApplication
 *
 * @author QIUCHANGQING620
 * @date 2020-02-18 12:05
 */
@SpringBootApplication
@MapperScan("com.minister.**.infra.mapper")
public class TestMybatisApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestMybatisApplication.class, args);
    }

}
