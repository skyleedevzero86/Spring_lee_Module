package com.sleekydz86.passykey.adapter.inbound.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleekydz86.passykey.application.dto.PasskeyRegistrationRequest;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.domain.port.inbound.CredentialManagementUseCase;
import com.sleekydz86.passykey.domain.port.inbound.UserUseCase;
import com.sleekydz86.passykey.domain.port.inbound.WebAuthnAuthenticationUseCase;
import com.sleekydz86.passykey.domain.port.inbound.WebAuthnRegistrationUseCase;
import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import com.webauthn4j.data.PublicKeyCredentialRequestOptions;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebAuthnController.class)
@DisplayName("WebAuthnController 테스트")
class WebAuthnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserUseCase userUseCase;

    @MockBean
    private WebAuthnRegistrationUseCase registrationUseCase;

    @MockBean
    private WebAuthnAuthenticationUseCase authenticationUseCase;

    @MockBean
    private CredentialManagementUseCase credentialManagementUseCase;

    private User user;
    private PasskeyRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        user = new User(
                "testuser",
                "encodedPassword",
                "test@example.com",
                "Test User",
                "userHandle123"
        );
        user.setId(1L);

        PasskeyRegistrationRequest.ResponseData responseData = new PasskeyRegistrationRequest.ResponseData();
        responseData.setAttestationObject("attestationObject");
        responseData.setClientDataJSON("clientDataJSON");
        responseData.setTransports(new String[]{"usb", "nfc"});

        PasskeyRegistrationRequest.CredentialData credentialData = new PasskeyRegistrationRequest.CredentialData();
        credentialData.setId("credentialId");
        credentialData.setRawId("rawId");
        credentialData.setType("public-key");
        credentialData.setResponse(responseData);

        PasskeyRegistrationRequest.PublicKeyData publicKeyData = new PasskeyRegistrationRequest.PublicKeyData();
        publicKeyData.setLabel("My Passkey");
        publicKeyData.setCredential(credentialData);

        registrationRequest = new PasskeyRegistrationRequest();
        registrationRequest.setPublicKey(publicKeyData);
    }

    @Test
    @DisplayName("등록 옵션 생성 성공")
    @WithMockUser(username = "testuser")
    void getRegistrationOptions_Success() throws Exception {
        // given
        PublicKeyCredentialCreationOptions options = mock(PublicKeyCredentialCreationOptions.class);
        when(userUseCase.findByUsername("testuser")).thenReturn(user);
        when(registrationUseCase.createRegistrationOptions(any(User.class), any(HttpSession.class)))
                .thenReturn(options);

        // when & then
        mockMvc.perform(post("/api/webauthn/register/options")
                        .sessionAttr("sessionId", "testSession"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("인증서 등록 성공")
    @WithMockUser(username = "testuser")
    void registerCredential_Success() throws Exception {
        // given
        when(userUseCase.findByUsername("testuser")).thenReturn(user);
        doNothing().when(registrationUseCase).registerCredential(
                any(User.class), anyString(), anyString(), anyString(), any(String[].class), any(HttpSession.class));

        // when & then
        mockMvc.perform(post("/api/webauthn/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest))
                        .sessionAttr("sessionId", "testSession"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    @DisplayName("인증 옵션 생성 성공")
    @WithMockUser(username = "testuser")
    void getAuthenticationOptions_Success() throws Exception {
        // given
        PublicKeyCredentialRequestOptions options = mock(PublicKeyCredentialRequestOptions.class);
        when(userUseCase.findByUsername("testuser")).thenReturn(user);
        when(authenticationUseCase.createAuthenticationOptions(any(User.class), any(HttpSession.class)))
                .thenReturn(options);

        // when & then
        mockMvc.perform(post("/api/webauthn/authenticate/options")
                        .sessionAttr("sessionId", "testSession"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("인증서 목록 조회 성공")
    @WithMockUser(username = "testuser")
    void getUserCredentials_Success() throws Exception {
        // given
        WebAuthnCredential credential = new WebAuthnCredential(
                "credentialId",
                "publicKeyCose",
                0L,
                "usb,nfc",
                user
        );
        credential.setId(1L);
        List<WebAuthnCredential> credentials = Arrays.asList(credential);

        when(userUseCase.findByUsername("testuser")).thenReturn(user);
        when(credentialManagementUseCase.getUserCredentials(user)).thenReturn(credentials);

        // when & then
        mockMvc.perform(get("/api/webauthn/credentials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].credentialId").value("credentialId"));
    }

    @Test
    @DisplayName("인증서 삭제 성공")
    @WithMockUser(username = "testuser")
    void deleteCredential_Success() throws Exception {
        // given
        when(userUseCase.findByUsername("testuser")).thenReturn(user);
        doNothing().when(credentialManagementUseCase).deleteCredential(anyString());

        // when & then
        mockMvc.perform(delete("/api/webauthn/credentials/credentialId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(credentialManagementUseCase, times(1)).deleteCredential("credentialId");
    }
}


