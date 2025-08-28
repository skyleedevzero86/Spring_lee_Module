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

import java.util.HashMap;
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
                "&response_type=code" +
                "&scope=talk_message";
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

            System.out.println("=== 메시지 전송 결과 ===");
            System.out.println("응답 코드: " + response.getStatusCode());
            System.out.println("응답 내용: " + responseBody);

            boolean success = responseBody != null && (Integer) responseBody.get("result_code") == 0;
            System.out.println("전송 성공 여부: " + success);

            if (success) {
                System.out.println("✅ 메시지 전송 성공! 카카오톡 '나와의 채팅'에서 확인하세요.");
            } else {
                System.out.println("❌ 메시지 전송 실패: " + responseBody);
            }

            return success;

        } catch (Exception e) {
            System.err.println("메시지 전송 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendScrapMessage(String accessToken, String url) {
        try {

            String domain = extractDomain(url);
            if (!isValidDomain(domain)) {
                System.err.println("❌ 허용되지 않은 도메인: " + domain);
                System.err.println("허용된 도메인: " + String.join(", ", getAllowedDomains()));
                System.err.println("카카오 개발자 콘솔에서 도메인을 등록하거나 허용된 도메인을 사용하세요.");
                return false;
            }

            System.out.println("✅ 도메인 검증 통과: " + domain);

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

            System.out.println("=== 스크랩 메시지 전송 결과 ===");
            System.out.println("요청 URL: " + url);
            System.out.println("도메인: " + domain);
            System.out.println("응답 코드: " + response.getStatusCode());
            System.out.println("응답 내용: " + responseBody);

            boolean success = responseBody != null && (Integer) responseBody.get("result_code") == 0;
            System.out.println("전송 성공 여부: " + success);

            if (success) {
                System.out.println("✅ 스크랩 메시지 전송 성공! 카카오톡 '나와의 채팅'에서 확인하세요.");
            } else {
                System.err.println("❌ 스크랩 메시지 전송 실패: " + responseBody);
            }

            return success;

        } catch (Exception e) {
            System.err.println("스크랩 메시지 전송 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String[] getAllowedDomains() {
        return new String[]{
                "developers.kakao.com",
                "www.daum.net",
                "daum.net",
                "localhost",
                "127.0.0.1"
        };
    }

    private boolean isValidDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return false;
        }

        String[] allowedDomains = getAllowedDomains();
        for (String allowedDomain : allowedDomains) {
            if (domain.equals(allowedDomain) || domain.endsWith("." + allowedDomain)) {
                return true;
            }
        }

        return false;
    }

    private String extractDomain(String url) {
        try {
            if (url == null || url.trim().isEmpty()) {
                return "";
            }

            String cleanUrl = url.trim();

            if (cleanUrl.startsWith("http://")) {
                cleanUrl = cleanUrl.substring(7);
            } else if (cleanUrl.startsWith("https://")) {
                cleanUrl = cleanUrl.substring(8);
            }

            int portIndex = cleanUrl.indexOf(':');
            if (portIndex != -1) {
                cleanUrl = cleanUrl.substring(0, portIndex);
            }

            int slashIndex = cleanUrl.indexOf('/');
            if (slashIndex != -1) {
                cleanUrl = cleanUrl.substring(0, slashIndex);
            }

            int queryIndex = cleanUrl.indexOf('?');
            if (queryIndex != -1) {
                cleanUrl = cleanUrl.substring(0, queryIndex);
            }

            return cleanUrl.toLowerCase();
        } catch (Exception e) {
            System.err.println("도메인 추출 실패: " + e.getMessage());
            return url;
        }
    }

    public Map<String, Object> getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    kakaoProperties.getApiUrl() + "/v2/user/me",
                    HttpMethod.GET, request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                Map<String, Object> userInfo = new HashMap<>();

                Object propertiesObj = responseBody.get("properties");
                if (propertiesObj instanceof Map) {
                    Map<String, Object> properties = (Map<String, Object>) propertiesObj;
                    userInfo.put("nickname", properties.get("nickname"));
                    userInfo.put("profileImage", properties.get("profile_image"));
                    userInfo.put("thumbnailImage", properties.get("thumbnail_image"));
                }

                Object kakaoAccountObj = responseBody.get("kakao_account");
                if (kakaoAccountObj instanceof Map) {
                    Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoAccountObj;

                    Object emailObj = kakaoAccount.get("email");
                    if (emailObj instanceof Map) {
                        Map<String, Object> emailMap = (Map<String, Object>) emailObj;
                        userInfo.put("email", emailMap.get("email"));
                    } else if (emailObj instanceof String) {
                        userInfo.put("email", emailObj);
                    }
                }

                userInfo.put("id", responseBody.get("id"));
                userInfo.put("connectedAt", responseBody.get("connected_at"));

                System.out.println("카카오 API 응답: " + responseBody);
                System.out.println("매핑된 사용자 정보: " + userInfo);

                return userInfo;
            }
            return null;
        } catch (Exception e) {
            System.err.println("사용자 정보 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}