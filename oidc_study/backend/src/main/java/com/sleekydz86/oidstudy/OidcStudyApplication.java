package com.sleekydz86.oidstudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OidcStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(OidcStudyApplication.class, args);
    }

}
