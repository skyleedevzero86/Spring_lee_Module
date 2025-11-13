package com.sleekydz86.payment2v2;

import com.sleekydz86.payment2v2.global.config.TossPaymentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TossPaymentProperties.class)
public class ToasPayment2V2Application {

    public static void main(String[] args) {
        SpringApplication.run(ToasPayment2V2Application.class, args);
    }

}
