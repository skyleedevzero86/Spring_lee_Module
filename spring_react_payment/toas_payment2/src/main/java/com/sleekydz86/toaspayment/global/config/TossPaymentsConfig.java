package com.sleekydz86.toaspayment.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "toss")
@Getter
@Setter
public class TossPaymentsConfig {
    private String approveUrl;
    private String refundUrl;
    private String secretApiKey;
    private String secureKey;
}
