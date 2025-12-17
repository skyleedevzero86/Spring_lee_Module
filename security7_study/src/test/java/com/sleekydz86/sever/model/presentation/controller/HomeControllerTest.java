package com.sleekydz86.sever.model.presentation.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@DisplayName("HomeController 테스트")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("홈 리다이렉트")
    void testHome_RedirectsToHomePage() throws Exception {
        // given
        // when
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
        // then
    }

    @Test
    @DisplayName("홈 페이지 접근 - 인증된 사용자")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testHomePage_Authenticated() throws Exception {
        // given
        // when
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("username"))
                .andExpect(model().attributeExists("authorities"));
        // then
    }

    @Test
    @DisplayName("사용자 페이지 접근 - USER 권한")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUserPage_WithUserRole() throws Exception {
        // given
        // when
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(view().name("user"))
                .andExpect(model().attributeExists("username"))
                .andExpect(model().attribute("role", "USER"));
        // then
    }

    @Test
    @DisplayName("사용자 페이지 접근 - ADMIN 권한")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUserPage_WithAdminRole() throws Exception {
        // given
        // when
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(view().name("user"));
        // then
    }

    @Test
    @DisplayName("관리자 페이지 접근 - ADMIN 권한")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminPage_WithAdminRole() throws Exception {
        // given
        // when
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("username"))
                .andExpect(model().attribute("role", "ADMIN"));
        // then
    }

    @Test
    @DisplayName("관리자 페이지 접근 - USER 권한 접근 거부")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testAdminPage_WithUserRole_AccessDenied() throws Exception {
        // given
        // when
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
        // then
    }

    @Test
    @DisplayName("접근 거부 페이지")
    @WithMockUser
    void testAccessDenied() throws Exception {
        // given
        // when
        mockMvc.perform(get("/access-denied"))
                .andExpect(status().isOk())
                .andExpect(view().name("access-denied"));
        // then
    }
}
