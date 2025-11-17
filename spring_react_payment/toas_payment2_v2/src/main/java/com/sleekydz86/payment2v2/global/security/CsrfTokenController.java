package com.sleekydz86.payment2v2.global.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/api/v1/csrf-token")
public class CsrfTokenController {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;

    @GetMapping
    public ResponseEntity<CsrfTokenResponse> getCsrfToken() {
        String token = generateToken();
        log.debug("CSRF 토큰 생성: {}", token);
        return ResponseEntity.ok(CsrfTokenResponse.builder()
                .token(token)
                .build());
    }

    @PostMapping("/validate")
    public ResponseEntity<CsrfTokenValidationResponse> validateCsrfToken(
            @RequestBody CsrfTokenRequest request,
            @RequestHeader(value = "X-CSRF-Token", required = false) String headerToken) {
        
        String tokenToValidate = request.getToken() != null ? request.getToken() : headerToken;
        
        if (tokenToValidate == null || tokenToValidate.isBlank()) {
            log.warn("CSRF 토큰 검증 실패: 토큰이 없음");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CsrfTokenValidationResponse.builder()
                            .valid(false)
                            .message("CSRF 토큰이 제공되지 않았습니다.")
                            .build());
        }

        // 간단한 토큰 형식 검증 (실제로는 세션/캐시에 저장된 토큰과 비교해야 함)
        boolean isValid = isValidTokenFormat(tokenToValidate);
        
        log.debug("CSRF 토큰 검증: token={}, valid={}", tokenToValidate, isValid);
        
        return ResponseEntity.ok(CsrfTokenValidationResponse.builder()
                .valid(isValid)
                .message(isValid ? "CSRF 토큰이 유효합니다." : "CSRF 토큰이 유효하지 않습니다.")
                .build());
    }

    private String generateToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private boolean isValidTokenFormat(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        // Base64 URL-safe 형식 검증
        try {
            Base64.getUrlDecoder().decode(token);
            return token.length() >= 32; // 최소 길이 검증
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CsrfTokenResponse {
        private String token;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CsrfTokenRequest {
        private String token;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CsrfTokenValidationResponse {
        private boolean valid;
        private String message;
    }
}



