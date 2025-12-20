package com.sleekydz86.passykey.adapter.inbound.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleekydz86.passykey.application.dto.RegisterRequest;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.port.inbound.UserUseCase;
import com.sleekydz86.passykey.global.security.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserUseCase userUseCase;

    @MockBean
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "testuser",
                "password123",
                "test@example.com",
                "Test User"
        );

        user = new User(
                "testuser",
                "encodedPassword",
                "test@example.com",
                "Test User",
                "userHandle123"
        );
        user.setId(1L);
    }

    @Test
    @DisplayName("사용자 등록 성공")
    void register_Success() throws Exception {
        // given
        when(userUseCase.register(any(RegisterRequest.class))).thenReturn(user);
        doNothing().when(authenticationService).setAuthentication(any(User.class), any());

        // when & then
        mockMvc.perform(post("/api/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("사용자 등록 실패 - 이미 존재하는 사용자명")
    void register_Fail_DuplicateUsername() throws Exception {
        // given
        when(userUseCase.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("이미 존재하는 사용자명입니다: testuser"));

        // when & then
        mockMvc.perform(post("/api/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("사용자명 중복 확인 - 존재함")
    void checkUsername_Exists() throws Exception {
        // given
        when(userUseCase.existsByUsername(anyString())).thenReturn(true);

        // when & then
        mockMvc.perform(get("/api/public/check-username")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("사용자명 중복 확인 - 존재하지 않음")
    void checkUsername_NotExists() throws Exception {
        // given
        when(userUseCase.existsByUsername(anyString())).thenReturn(false);

        // when & then
        mockMvc.perform(get("/api/public/check-username")
                        .param("username", "newuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("이메일 중복 확인 - 존재함")
    void checkEmail_Exists() throws Exception {
        // given
        when(userUseCase.existsByEmail(anyString())).thenReturn(true);

        // when & then
        mockMvc.perform(get("/api/public/check-email")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("이메일 중복 확인 - 존재하지 않음")
    void checkEmail_NotExists() throws Exception {
        // given
        when(userUseCase.existsByEmail(anyString())).thenReturn(false);

        // when & then
        mockMvc.perform(get("/api/public/check-email")
                        .param("email", "new@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));
    }
}


