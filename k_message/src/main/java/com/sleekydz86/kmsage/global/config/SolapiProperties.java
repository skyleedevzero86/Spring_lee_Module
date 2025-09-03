package com.sleekydz86.kmsage.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "solapi")
public class SolapiProperties {
    private String apiKey;
    private String apiSecret;
    private String baseUrl;
    private String defaultFromNumber;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getDefaultFromNumber() { return defaultFromNumber; }
    public void setDefaultFromNumber(String defaultFromNumber) { this.defaultFromNumber = defaultFromNumber; }
}