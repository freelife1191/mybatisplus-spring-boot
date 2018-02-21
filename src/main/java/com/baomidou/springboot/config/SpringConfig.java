package com.baomidou.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.baomidou.springboot.excel.ExcelContext;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: CuiCan
 * @Date: 2017-6-11
 * @Time: 12:36
 * @Description:
 */
@Configuration
@ComponentScan(basePackages = "com.baomidou.springboot.config")
// 검사 패키지 구성
@PropertySource(value = {"classpath:jdbc.properties"}, ignoreResourceNotFound = true)
public class SpringConfig {

    @Bean
    public ExcelContext excelContext() {
        return new ExcelContext("excel-config.xml");
    }

}


