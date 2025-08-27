package com.sleekydz86.kmsage.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleekydz86.kmsage.domain.dto.MessageTemplate;
import com.sleekydz86.kmsage.global.config.KakaoProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class KakaoService {

    @Autowired
    private KakaoProperties kakaoProperties;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getKakaoLoginUrl() {
        return kakaoProperties.getAuthUrl() +
                "?client_id=" + kakaoProperties.getClientId() +
                "&redirect_uri=" + kakaoProperties.getRedirectUri() +
                "&response_type=code";
    }

    public String getAccessToken(String authCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoProperties.getClientId());
        params.add("redirect_uri", kakaoProperties.getRedirectUri());
        params.add("code", authCode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    kakaoProperties.getTokenUrl(), request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            return (String) responseBody.get("access_token");
        } catch (Exception e) {
            throw new RuntimeException("토큰 획득 실패: " + e.getMessage());
        }
    }

    public boolean sendMessageToMe(String accessToken, MessageTemplate template) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(accessToken);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            String templateJson = objectMapper.writeValueAsString(template);
            params.add("template_object", templateJson);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    kakaoProperties.getApiUrl() + "/v2/api/talk/memo/default/send",
                    request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            return responseBody != null && (Integer) responseBody.get("result_code") == 0;

        } catch (Exception e) {
            System.err.println("메시지 전송 실패: " + e.getMessage());
            return false;
        }
    }

    public boolean sendScrapMessage(String accessToken, String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(accessToken);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("request_url", url);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    kakaoProperties.getApiUrl() + "/v2/api/talk/memo/scrap/send",
                    request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            return responseBody != null && (Integer) responseBody.get("result_code") == 0;

        } catch (Exception e) {
            System.err.println("스크랩 메시지 전송 실패: " + e.getMessage());
            return false;
        }
    }
}