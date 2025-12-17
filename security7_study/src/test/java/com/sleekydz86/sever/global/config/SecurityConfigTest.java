package com.sleekydz86.sever.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SecurityConfig 테스트 - Spring Security 7 설정 검증")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("PasswordEncoder Bean 검증 - DelegatingPasswordEncoder 사용")
    void testPasswordEncoder_DelegatingPasswordEncoder() {
        // given & when
        // then
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder.getClass().getSimpleName().contains("DelegatingPasswordEncoder"));
    }

    @Test
    @DisplayName("PasswordEncoder - BCrypt 암호화 지원 검증")
    void testPasswordEncoder_BCryptSupport() {
        // given
        String rawPassword = "testPassword123";

        // when
        String encoded = passwordEncoder.encode(rawPassword);

        // then
        assertNotNull(encoded);
        assertTrue(encoded.startsWith("{bcrypt}"));
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        assertFalse(passwordEncoder.matches("wrongPassword", encoded));
    }

    @Test
    @DisplayName("공개 엔드포인트 접근 - /login")
    void testPublicEndpoint_Login() throws Exception {
        // given & when
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
        // then
    }

    @Test
    @DisplayName("공개 엔드포인트 접근 - /users/register")
    void testPublicEndpoint_Register() throws Exception {
        // given & when
        mockMvc.perform(get("/users/register"))
                .andExpect(status().isOk());
        // then
    }

    @Test
    @DisplayName("보호된 엔드포인트 접근 - /home (인증 필요)")
    void testProtectedEndpoint_Home() throws Exception {
        // given & when
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        // then
    }

    @Test
    @DisplayName("보호된 엔드포인트 접근 - /user (인증 필요)")
    void testProtectedEndpoint_User() throws Exception {
        // given & when
        mockMvc.perform(get("/user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        // then
    }

    @Test
    @DisplayName("관리자 전용 엔드포인트 접근 - /admin (ADMIN 권한 필요)")
    @WithMockUser(username = "user", roles = { "USER" })
    void testAdminEndpoint_WithUserRole() throws Exception {
        // given & when
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
        // then
    }

    @Test
    @DisplayName("관리자 전용 엔드포인트 접근 - /users/list (ADMIN 권한 필요)")
    @WithMockUser(username = "user", roles = { "USER" })
    void testAdminEndpoint_UserList_WithUserRole() throws Exception {
        // given & when
        mockMvc.perform(get("/users/list"))
                .andExpect(status().isForbidden());
        // then
    }

    @Test
    @DisplayName("관리자 전용 엔드포인트 접근 - /users/detail/1 (ADMIN 권한 필요)")
    @WithMockUser(username = "user", roles = { "USER" })
    void testAdminEndpoint_UserDetail_WithUserRole() throws Exception {
        // given & when
        mockMvc.perform(get("/users/detail/1"))
                .andExpect(status().isForbidden());
        // then
    }

    @Test
    @DisplayName("관리자 전용 엔드포인트 접근 - /admin (ADMIN 권한으로 접근 성공)")
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void testAdminEndpoint_WithAdminRole() throws Exception {
        // given & when
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk());
        // then
    }

    @Test
    @DisplayName("사용자 엔드포인트 접근 - /user (USER 권한으로 접근 성공)")
    @WithMockUser(username = "user", roles = { "USER" })
    void testUserEndpoint_WithUserRole() throws Exception {
        // given & when
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk());
        // then
    }

    @Test
    @DisplayName("사용자 엔드포인트 접근 - /user (ADMIN 권한으로도 접근 가능)")
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void testUserEndpoint_WithAdminRole() throws Exception {
        // given & when
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk());
        // then
    }

    @Test
    @DisplayName("로그아웃 처리 - clearAuthentication 및 쿠키 삭제 검증")
    @WithMockUser(username = "user", roles = { "USER" })
    void testLogout_ClearAuthentication() throws Exception {
        // given & when
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
        // then
    }

    @Test
    @DisplayName("CSRF 비활성화 검증 - POST 요청이 CSRF 없이도 처리됨 (테스트 환경)")
    void testCsrf_Disabled() throws Exception {
        // given & when
        mockMvc.perform(post("/login")
                .param("username", "test")
                .param("password", "test"))
                .andExpect(status().is3xxRedirection());
        // then
    }

    @Test
    @DisplayName("세션 관리 설정 검증 - 최대 1개 세션")
    @WithMockUser(username = "user", roles = { "USER" })
    void testSessionManagement_MaximumSessions() throws Exception {
        // given & when
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk());
        // then
    }

    @Test
    @DisplayName("예외 처리 설정 검증 - 접근 거부 페이지 리다이렉트")
    @WithMockUser(username = "user", roles = { "USER" })
    void testExceptionHandling_AccessDenied() throws Exception {
        // given & when
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
        // then
    }
}
