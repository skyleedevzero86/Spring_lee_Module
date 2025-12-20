package com.sleekydz86.passykey.global.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webauthn4j.data.client.Origin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public final class ClientDataJSONParser {
    private static final Logger logger = LoggerFactory.getLogger(ClientDataJSONParser.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ClientDataJSONParser() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Origin extractOrigin(byte[] clientDataJSONBytes) {
        try {
            String clientDataJSONString = new String(clientDataJSONBytes, StandardCharsets.UTF_8);
            JsonNode jsonNode = objectMapper.readTree(clientDataJSONString);
            String originString = jsonNode.get("origin").asText();
            return new Origin(originString);
        } catch (Exception e) {
            logger.error("clientDataJSON에서 Origin 추출 실패", e);
            throw new RuntimeException("Origin 추출 실패: " + e.getMessage(), e);
        }
    }

    public static String extractOriginString(byte[] clientDataJSONBytes) {
        try {
            String clientDataJSONString = new String(clientDataJSONBytes, StandardCharsets.UTF_8);
            JsonNode jsonNode = objectMapper.readTree(clientDataJSONString);
            String origin = jsonNode.get("origin").asText();
            
            try {
                java.net.URL url = new java.net.URL(origin);
                return url.getHost();
            } catch (Exception e) {
                logger.debug("Origin URL 파싱 실패, 원본 반환: {}", origin);
                return origin;
            }
        } catch (Exception e) {
            logger.error("clientDataJSON에서 Origin 추출 실패", e);
            throw new RuntimeException("Origin 추출 실패: " + e.getMessage(), e);
        }
    }
}

