package com.sleekydevz86.tts.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tts.google")
public record GoogleCredentialProperties(String credentialsFile) {

    public GoogleCredentialProperties {
        if (credentialsFile == null) {
            credentialsFile = "";
        }
    }
}
