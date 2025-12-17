package com.sleekydz86.sever.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security 통합 테스트")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("공개 엔드포인트 접근 - 인증 없이 접근 가능")
    void testPublicEndpoints_AccessibleWithoutAuth() throws Exception {
        // given
        // when
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
        // then
    }

    @Test
    @DisplayName("보호된 엔드포인트 접근 - 인증 필요")
    void testProtectedEndpoints_RequireAuth() throws Exception {
        // given
        // when
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        // then
    }

    @Test
    @DisplayName("사용자 페이지 접근 - USER 권한")
    @WithMockUser(username = "user", roles = {"USER"})
    void testUserRole_CanAccessUserPage() throws Exception {
        // given
        // when
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(view().name("user"));
        // then
    }

    @Test
    @DisplayName("관리자 페이지 접근 - USER 권한 접근 거부")
    @WithMockUser(username = "user", roles = {"USER"})
    void testUserRole_CannotAccessAdminPage() throws Exception {
        // given
        // when
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
        // then
    }

    @Test
    @DisplayName("모든 페이지 접근 - ADMIN 권한")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminRole_CanAccessAllPages() throws Exception {
        // given
        // when
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"));
        // then
    }

    @Test
    @DisplayName("로그아웃 처리")
    @WithMockUser(username = "user", roles = {"USER"})
    void testLogout() throws Exception {
        // given
        // when
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
        // then
    }

    @Test
    @DisplayName("로그인 처리 - Spring Security가 PasswordEncoder로 비밀번호 검증")
    void testLogin_WithPasswordEncoder() throws Exception {
        // given
        // when
        mockMvc.perform(post("/login")
                        .param("username", "user")
                        .param("password", "password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
        // then
    }
}
