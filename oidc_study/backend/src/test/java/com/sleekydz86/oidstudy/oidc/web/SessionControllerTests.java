package com.sleekydz86.oidstudy.oidc.web;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SessionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousUserShouldReceiveAnonymousPayload() throws Exception {
        mockMvc.perform(get("/api/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.loginUrl").value("/oauth2/authorization/naver"));
    }

    @Test
    void unauthorizedDashboardShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mockedOidcSessionShouldExposeAuthenticatedState() throws Exception {
        mockMvc.perform(
                        get("/api/session").with(
                                oidcLogin().idToken(token -> token
                                        .claim("sub", "mock-user")
                                        .claim("name", "Mock User")
                                        .claim("email", "mock@example.com")
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true));
    }
}