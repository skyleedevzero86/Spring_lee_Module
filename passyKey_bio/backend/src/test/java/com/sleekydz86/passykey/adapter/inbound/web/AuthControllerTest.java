package com.sleekydz86.passykey.adapter.inbound.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleekydz86.passykey.application.dto.PasskeyAuthenticationRequest;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.port.inbound.UserUseCase;
import com.sleekydz86.passykey.domain.port.inbound.WebAuthnAuthenticationUseCase;
import com.sleekydz86.passykey.global.security.AuthenticationService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserUseCase userUseCase;

    @MockBean
    private WebAuthnAuthenticationUseCase authenticationUseCase;

    @MockBean
    private AuthenticationService authenticationService;

    private PasskeyAuthenticationRequest authRequest;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(
                "testuser",
                "encodedPassword",
                "test@example.com",
                "Test User",
                "userHandle123");
        user.setId(1L);

        PasskeyAuthenticationRequest.AuthenticationResponseData responseData = new PasskeyAuthenticationRequest.AuthenticationResponseData();
        responseData.setAuthenticatorData("authenticatorData");
        responseData.setClientDataJSON("clientDataJSON");
        responseData.setSignature("signature");
        responseData.setUserHandle("userHandle");

        authRequest = new PasskeyAuthenticationRequest();
        authRequest.setId("credentialId");
        authRequest.setRawId("rawId");
        authRequest.setResponse(responseData);
    }

    @Test
    @DisplayName("패스키 인증 성공")
    void authenticateWithPasskey_Success() throws Exception {
        // given
        when(authenticationUseCase.authenticate(
                anyString(), anyString(), anyString(), anyString(), any(), any(HttpSession.class)))
                .thenReturn(user);
        doNothing().when(authenticationService).setAuthentication(any(User.class), any());

        // when & then
        mockMvc.perform(post("/api/auth/webauthn/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest))
                .sessionAttr("sessionId", "testSession"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.redirectUrl").exists())
                .andExpect(jsonPath("$.data.authenticated").value(true));

        verify(authenticationService, times(1)).setAuthentication(any(User.class), any());
    }

    @Test
    @DisplayName("패스키 인증 실패")
    void authenticateWithPasskey_Fail() throws Exception {
        // given
        when(authenticationUseCase.authenticate(
                anyString(), anyString(), anyString(), anyString(), any(), any(HttpSession.class)))
                .thenThrow(new RuntimeException("인증 실패"));

        // when & then
        mockMvc.perform(post("/api/auth/webauthn/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest))
                .sessionAttr("sessionId", "testSession"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() throws Exception {
        // given
        doNothing().when(authenticationService).clearAuthentication();

        // when & then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authenticationService, times(1)).clearAuthentication();
    }
}

