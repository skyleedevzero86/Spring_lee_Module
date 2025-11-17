package com.sleekydz86.toaspayment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleekydz86.toaspayment.application.dto.LoginRequest;
import com.sleekydz86.toaspayment.application.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("인증 통합 테스트")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공")
    void register_success() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "password123",
                "테스트 사용자"
        );

        // when & then
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("테스트 사용자"));
    }

    @Test
    @DisplayName("중복 이메일 회원가입 실패")
    void register_duplicateEmail_fail() throws Exception {
        // given
        RegisterRequest firstRequest = new RegisterRequest(
                "duplicate@example.com",
                "password123",
                "첫 번째 사용자"
        );
        RegisterRequest duplicateRequest = new RegisterRequest(
                "duplicate@example.com",
                "password456",
                "두 번째 사용자"
        );

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)));

        // when & then
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {
        // given
        RegisterRequest registerRequest = new RegisterRequest(
                "login@example.com",
                "password123",
                "로그인 사용자"
        );
        LoginRequest loginRequest = new LoginRequest(
                "login@example.com",
                "password123"
        );

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data.email").value("login@example.com"))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    @DisplayName("잘못된 비밀번호 로그인 실패")
    void login_wrongPassword_fail() throws Exception {
        // given
        RegisterRequest registerRequest = new RegisterRequest(
                "wrong@example.com",
                "password123",
                "잘못된 비밀번호 사용자"
        );
        LoginRequest loginRequest = new LoginRequest(
                "wrong@example.com",
                "wrongpassword"
        );

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 이메일 로그인 실패")
    void login_notFoundEmail_fail() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest(
                "notfound@example.com",
                "password123"
        );

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 일치하지 않습니다."));
    }
}





