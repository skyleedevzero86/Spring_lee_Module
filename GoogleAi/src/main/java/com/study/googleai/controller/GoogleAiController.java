package com.study.googleai.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
public class GoogleAiController {

    private final String apiKey;
    private final String baseUrl;
    private final RestClient restClient;

    public GoogleAiController(
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.chat.base-url}") String baseUrl,
            RestClient.Builder builder) {

        this.apiKey = apiKey;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.restClient = builder.baseUrl(this.baseUrl).build();
    }

    @GetMapping("/models")
    public Map<String, Object> getModels() {
        ResponseEntity<Map> response = restClient.get()
                .uri("v1beta/openai/models")
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .toEntity(Map.class);
        return response.getBody();
    }
}