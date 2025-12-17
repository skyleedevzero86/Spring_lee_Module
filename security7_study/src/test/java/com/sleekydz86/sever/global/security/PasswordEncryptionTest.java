package com.sleekydz86.sever.global.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("비밀번호 암호화 테스트")
class PasswordEncryptionTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("비밀번호 암호화 형식 검증")
    void testPasswordEncryption_Format() {
        // given
        String rawPassword = "testpassword123";

        // when
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // then
        assertNotNull(encodedPassword);
        assertTrue(encodedPassword.startsWith("{bcrypt}"));
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.length() > 50);
    }

    @Test
    @DisplayName("비밀번호 매칭 검증")
    void testPasswordEncryption_Match() {
        // given
        String rawPassword = "testpassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // when & then
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertFalse(passwordEncoder.matches("wrongpassword", encodedPassword));
    }

    @Test
    @DisplayName("비밀번호 암호화 고유성 검증")
    void testPasswordEncryption_UniqueEncoding() {
        // given
        String rawPassword = "samepassword";

        // when
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);

        // then
        assertNotEquals(encoded1, encoded2);
        assertTrue(passwordEncoder.matches(rawPassword, encoded1));
        assertTrue(passwordEncoder.matches(rawPassword, encoded2));
    }

    @Test
    @DisplayName("서로 다른 비밀번호 암호화 검증")
    void testPasswordEncryption_DifferentPasswords() {
        // given
        String password1 = "password1";
        String password2 = "password2";

        // when
        String encoded1 = passwordEncoder.encode(password1);
        String encoded2 = passwordEncoder.encode(password2);

        // then
        assertNotEquals(encoded1, encoded2);
        assertTrue(passwordEncoder.matches(password1, encoded1));
        assertTrue(passwordEncoder.matches(password2, encoded2));
        assertFalse(passwordEncoder.matches(password1, encoded2));
        assertFalse(passwordEncoder.matches(password2, encoded1));
    }

    @Test
    @DisplayName("특수문자 포함 비밀번호 암호화")
    void testPasswordEncryption_SpecialCharacters() {
        // given
        String passwordWithSpecialChars = "P@ssw0rd!@#$%^&*()";

        // when
        String encodedPassword = passwordEncoder.encode(passwordWithSpecialChars);

        // then
        assertTrue(encodedPassword.startsWith("{bcrypt}"));
        assertTrue(passwordEncoder.matches(passwordWithSpecialChars, encodedPassword));
    }

    @Test
    @DisplayName("긴 비밀번호 암호화")
    void testPasswordEncryption_LongPassword() {
        // given
        String longPassword = "a".repeat(100);

        // when
        String encodedPassword = passwordEncoder.encode(longPassword);

        // then
        assertTrue(encodedPassword.startsWith("{bcrypt}"));
        assertTrue(passwordEncoder.matches(longPassword, encodedPassword));
    }
}
