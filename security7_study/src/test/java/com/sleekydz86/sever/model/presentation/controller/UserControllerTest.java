package com.sleekydz86.sever.model.presentation.controller;

import com.sleekydz86.sever.model.application.service.UserService;
import com.sleekydz86.sever.model.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "{bcrypt}$2a$10$encoded", true, Arrays.asList("ROLE_USER"));
        when(passwordEncoder.encode(anyString())).thenReturn("{bcrypt}$2a$10$encodedPassword");
    }

    @Test
    @DisplayName("회원가입 폼 접근 - 비로그인 사용자 접근 가능")
    void testRegisterForm_AccessibleWithoutAuth() throws Exception {
        // given
        // when
        mockMvc.perform(get("/users/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
        // then
    }

    @Test
    @DisplayName("회원가입 성공 - 비밀번호 암호화 검증")
    void testRegister_Success() throws Exception {
        // given
        doNothing().when(userService).register(anyString(), anyString(), anyString());

        // when
        mockMvc.perform(post("/users/register")
                        .param("username", "newuser")
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("message"));

        // then
        verify(userService, times(1)).register(eq("newuser"), anyString(), eq("ROLE_USER"));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    @DisplayName("회원가입 - 암호화된 비밀번호 전달 검증")
    void testRegister_WithEncryptedPassword() throws Exception {
        // given
        doNothing().when(userService).register(anyString(), anyString(), anyString());

        // when
        mockMvc.perform(post("/users/register")
                        .param("username", "newuser")
                        .param("password", "password123")
                        .with(csrf()));

        // then
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(userService).register(anyString(), passwordCaptor.capture(), anyString());
        assertTrue(passwordCaptor.getValue().startsWith("{bcrypt}"));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 사용자명")
    void testRegister_Failure() throws Exception {
        // given
        doThrow(new RuntimeException("Username already exists")).when(userService)
                .register(anyString(), anyString(), anyString());

        // when
        mockMvc.perform(post("/users/register")
                        .param("username", "existinguser")
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/register"))
                .andExpect(flash().attributeExists("error"));
        // then
    }

    @Test
    @DisplayName("내 정보 조회 - 인증된 사용자")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testProfile_Authenticated() throws Exception {
        // given
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);

        // when
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"));
        // then
    }

    @Test
    @DisplayName("내 정보 조회 - 비인증 사용자 접근 거부")
    void testProfile_Unauthenticated() throws Exception {
        // given
        // when
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        // then
    }

    @Test
    @DisplayName("정보 수정 폼 - 인증된 사용자")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testEditForm_Authenticated() throws Exception {
        // given
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);

        // when
        mockMvc.perform(get("/users/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit"))
                .andExpect(model().attributeExists("user"));
        // then
    }

    @Test
    @DisplayName("정보 수정 - 새 비밀번호로 업데이트")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdate_WithNewPassword() throws Exception {
        // given
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        doNothing().when(userService).updateUser(anyLong(), isNull(), anyString(), isNull(), isNull());

        // when
        mockMvc.perform(post("/users/edit")
                        .param("password", "newpassword123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/profile"))
                .andExpect(flash().attributeExists("message"));

        // then
        verify(userService, times(1)).updateUser(eq(1L), isNull(), anyString(), isNull(), isNull());
        verify(passwordEncoder, times(1)).encode("newpassword123");
    }

    @Test
    @DisplayName("정보 수정 - 암호화된 비밀번호 전달 검증")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdate_WithEncryptedPassword() throws Exception {
        // given
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        doNothing().when(userService).updateUser(anyLong(), isNull(), anyString(), isNull(), isNull());

        // when
        mockMvc.perform(post("/users/edit")
                        .param("password", "newpassword123")
                        .with(csrf()));

        // then
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(userService).updateUser(anyLong(), isNull(), passwordCaptor.capture(), isNull(), isNull());
        assertTrue(passwordCaptor.getValue().startsWith("{bcrypt}"));
    }

    @Test
    @DisplayName("정보 수정 - 비밀번호 없이 업데이트")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdate_WithoutPassword() throws Exception {
        // given
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        doNothing().when(userService).updateUser(anyLong(), isNull(), isNull(), isNull(), isNull());

        // when
        mockMvc.perform(post("/users/edit")
                        .param("password", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/profile"));

        // then
        verify(userService, times(1)).updateUser(eq(1L), isNull(), isNull(), isNull(), isNull());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDelete_Success() throws Exception {
        // given
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        doNothing().when(userService).deleteUser(anyLong());

        // when
        mockMvc.perform(post("/users/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logout"))
                .andExpect(flash().attributeExists("message"));

        // then
        verify(userService, times(1)).deleteUser(eq(1L));
    }

    @Test
    @DisplayName("회원 목록 조회 - 관리자 권한")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testList_WithAdminRole() throws Exception {
        // given
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", 1L);
        user1.put("username", "user1");
        user1.put("authorities", "ROLE_USER");

        when(userService.getUserList()).thenReturn(Arrays.asList(user1));

        // when
        mockMvc.perform(get("/users/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-list"))
                .andExpect(model().attributeExists("users"));
        // then
    }

    @Test
    @DisplayName("회원 검색 - 키워드로 검색")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testList_WithSearchKeyword() throws Exception {
        // given
        String keyword = "test";
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1L);
        user.put("username", "testuser");
        user.put("authorities", "ROLE_USER");

        when(userService.searchUsers(keyword)).thenReturn(Arrays.asList(user));

        // when
        mockMvc.perform(get("/users/list")
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(view().name("user-list"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("keyword", keyword));

        // then
        verify(userService, times(1)).searchUsers(keyword);
    }

    @Test
    @DisplayName("회원 목록 조회 - 일반 사용자 권한 접근 거부")
    @WithMockUser(username = "user", roles = {"USER"})
    void testList_WithUserRole_AccessDenied() throws Exception {
        // given
        // when
        mockMvc.perform(get("/users/list"))
                .andExpect(status().isForbidden());
        // then
    }

    @Test
    @DisplayName("회원 상세보기 - 관리자 권한")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDetail_WithAdminRole() throws Exception {
        // given
        when(userService.getUserById(1L)).thenReturn(testUser);

        // when
        mockMvc.perform(get("/users/detail/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-detail"))
                .andExpect(model().attributeExists("user"));
        // then
    }

    @Test
    @DisplayName("회원 상세보기 - 사용자 없음")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDetail_UserNotFound() throws Exception {
        // given
        when(userService.getUserById(999L)).thenReturn(null);

        // when
        mockMvc.perform(get("/users/detail/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/list"));
        // then
    }

    @Test
    @DisplayName("회원 상세보기 - 일반 사용자 권한 접근 거부")
    @WithMockUser(username = "user", roles = {"USER"})
    void testDetail_WithUserRole_AccessDenied() throws Exception {
        // given
        // when
        mockMvc.perform(get("/users/detail/1"))
                .andExpect(status().isForbidden());
        // then
    }
}
