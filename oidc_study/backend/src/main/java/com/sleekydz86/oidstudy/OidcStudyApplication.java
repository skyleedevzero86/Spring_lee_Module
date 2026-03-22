package com.sleekydz86.oidstudy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.sleekydz86.oidstudy.oidc.mapper")
public class OidcStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(OidcStudyApplication.class, args);
    }

}
