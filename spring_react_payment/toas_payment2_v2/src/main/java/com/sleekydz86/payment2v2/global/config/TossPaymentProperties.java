package com.sleekydz86.payment2v2.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "toss")
public class TossPaymentProperties {

    private Api api = new Api();

    @Getter
    @Setter
    public static class Api {
        private String key;
    }
}
