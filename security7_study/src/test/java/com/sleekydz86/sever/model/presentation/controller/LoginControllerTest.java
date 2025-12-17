package com.sleekydz86.sever.model.presentation.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@DisplayName("LoginController 테스트")
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("로그인 페이지 접근 - 파라미터 없음")
    void testLoginPage_WithoutParameters() throws Exception {
        // given
        // when
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
        // then
    }

    @Test
    @DisplayName("로그인 페이지 접근 - 에러 파라미터")
    void testLoginPage_WithError() throws Exception {
        // given
        // when
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "사용자명 또는 비밀번호가 올바르지 않습니다"));
        // then
    }

    @Test
    @DisplayName("로그인 페이지 접근 - 로그아웃 파라미터")
    void testLoginPage_WithLogout() throws Exception {
        // given
        // when
        mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("message", "로그아웃되었습니다"));
        // then
    }

    @Test
    @DisplayName("로그인 페이지 접근 - 에러 및 로그아웃 파라미터 동시")
    void testLoginPage_WithBothParameters() throws Exception {
        // given
        // when
        mockMvc.perform(get("/login")
                        .param("error", "true")
                        .param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("message"));
        // then
    }
}
