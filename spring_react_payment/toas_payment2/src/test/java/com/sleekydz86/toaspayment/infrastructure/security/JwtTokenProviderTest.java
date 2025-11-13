package com.sleekydz86.toaspayment.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JWT Token Provider 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String secretKey;
    private long expiration;

    @BeforeEach
    void setUp() {
        secretKey = "testSecretKey12345678901234567890123456789012345678901234567890";
        expiration = 86400000L;
        jwtTokenProvider = new JwtTokenProvider(secretKey, expiration);
    }

    @Test
    @DisplayName("토큰 생성 성공")
    void generateToken_success() {
        //given
        Long userId = 1L;
        String email = "test@example.com";

        //when
        String token = jwtTokenProvider.generateToken(userId, email);

        //then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.").length).isEqualTo(3);
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출 성공")
    void getUserIdFromToken_success() {
        //given
        Long userId = 1L;
        String email = "test@example.com";
        String token = jwtTokenProvider.generateToken(userId, email);

        //when
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        //then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("토큰에서 이메일 추출 성공")
    void getEmailFromToken_success() {
        //given
        Long userId = 1L;
        String email = "test@example.com";
        String token = jwtTokenProvider.generateToken(userId, email);

        //when
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        //then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void validateToken_success() {
        //given
        Long userId = 1L;
        String email = "test@example.com";
        String token = jwtTokenProvider.generateToken(userId, email);

        //when
        boolean isValid = jwtTokenProvider.validateToken(token);

        //then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 실패")
    void validateToken_fail() {
        //given
        String invalidToken = "invalid.token.here";

        //when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        //then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("빈 토큰 검증 실패")
    void validateToken_fail_empty() {
        //given
        String emptyToken = "";

        //when
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        //then
        assertThat(isValid).isFalse();
    }
}



