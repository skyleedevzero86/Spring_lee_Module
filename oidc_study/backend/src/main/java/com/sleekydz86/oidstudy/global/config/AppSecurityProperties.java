package com.sleekydz86.oidstudy.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
        String bootstrapAdminSub,
        String bootstrapAdminEmail
) {
}