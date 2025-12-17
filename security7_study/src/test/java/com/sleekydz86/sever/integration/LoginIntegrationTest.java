package com.sleekydz86.sever.integration;

import com.sleekydz86.sever.global.security.CustomUserDetailsService;
import com.sleekydz86.sever.model.application.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("로그인 통합 테스트 - 암호화 검증")
class LoginIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("로그인 프로세스 - 평문 비밀번호와 암호화된 비밀번호 비교 검증")
    void testLoginProcess_PasswordMatching() {
        // given
        String username = "logintest" + System.currentTimeMillis();
        String rawPassword = "loginpassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        userService.register(username, encodedPassword, "ROLE_USER");

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // then
        assertNotNull(userDetails);
        assertTrue(userDetails.getPassword().startsWith("{bcrypt}"));

        boolean passwordMatches = passwordEncoder.matches(rawPassword, userDetails.getPassword());
        assertTrue(passwordMatches);

        boolean wrongPasswordMatches = passwordEncoder.matches("wrongpassword", userDetails.getPassword());
        assertFalse(wrongPasswordMatches);
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void testLoginFailure_WrongPassword() {
        // given
        String username = "loginfail" + System.currentTimeMillis();
        String correctPassword = "correct123";
        String encodedPassword = passwordEncoder.encode(correctPassword);

        userService.register(username, encodedPassword, "ROLE_USER");
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // when
        boolean correctMatch = passwordEncoder.matches(correctPassword, userDetails.getPassword());
        boolean wrongMatch = passwordEncoder.matches("wrongpassword", userDetails.getPassword());

        // then
        assertTrue(correctMatch);
        assertFalse(wrongMatch);
    }

    @Test
    @DisplayName("로그인 성공 - 암호화된 비밀번호 형식 검증")
    void testLoginSuccess_EncryptedPasswordFormat() {
        // given
        String username = "loginformat" + System.currentTimeMillis();
        String rawPassword = "format123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        userService.register(username, encodedPassword, "ROLE_USER");

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // then
        assertNotNull(userDetails.getPassword());
        assertTrue(userDetails.getPassword().startsWith("{bcrypt}"));
        assertTrue(userDetails.getPassword().length() > 50);
        assertTrue(passwordEncoder.matches(rawPassword, userDetails.getPassword()));
    }

    @Test
    @DisplayName("로그인 시 암호화 검증 흐름 - 전체 프로세스")
    void testLoginProcess_FullFlow() {
        // given
        String username = "loginflow" + System.currentTimeMillis();
        String userInputPassword = "flowpassword123";
        String encodedPasswordForDB = passwordEncoder.encode(userInputPassword);

        userService.register(username, encodedPasswordForDB, "ROLE_USER");

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        String encryptedPasswordFromDB = userDetails.getPassword();
        boolean loginSuccess = passwordEncoder.matches(userInputPassword, encryptedPasswordFromDB);

        // then
        assertTrue(encryptedPasswordFromDB.startsWith("{bcrypt}"));
        assertTrue(loginSuccess);
        assertNotEquals(userInputPassword, encryptedPasswordFromDB);
    }
}

