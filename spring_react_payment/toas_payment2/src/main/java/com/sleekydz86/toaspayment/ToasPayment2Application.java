package com.sleekydz86.toaspayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class ToasPayment2Application {

    public static void main(String[] args) {
        SpringApplication.run(ToasPayment2Application.class, args);
    }

}
