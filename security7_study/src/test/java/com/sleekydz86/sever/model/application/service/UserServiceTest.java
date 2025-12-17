package com.sleekydz86.sever.model.application.service;

import com.sleekydz86.sever.model.domain.User;
import com.sleekydz86.sever.model.infrastructure.persistence.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "{bcrypt}$2a$10$encoded", true, Arrays.asList("ROLE_USER"));
    }

    @Test
    @DisplayName("사용자명으로 사용자 조회 성공")
    void testFindByUsername_Success() {
        // given
        String username = "testuser";
        when(userMapper.findByUsername(username)).thenReturn(testUser);

        // when
        User result = userService.findByUsername(username);

        // then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertTrue(result.getPassword().startsWith("{bcrypt}"));
        assertTrue(result.isEnabled());
        verify(userMapper, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("사용자명으로 사용자 조회 실패 - 사용자 없음")
    void testFindByUsername_NotFound() {
        // given
        String username = "nonexistent";
        when(userMapper.findByUsername(username)).thenReturn(null);

        // when
        User result = userService.findByUsername(username);

        // then
        assertNull(result);
        verify(userMapper, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("회원가입 - 암호화된 비밀번호로 사용자 생성")
    void testRegister_WithEncryptedPassword() {
        // given
        String username = "newuser";
        String encodedPassword = "{bcrypt}$2a$10$encodedPassword";
        String authority = "ROLE_USER";

        // when
        userService.register(username, encodedPassword, authority);

        // then
        ArgumentCaptor<String> operationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> authorityCaptor = ArgumentCaptor.forClass(String.class);

        verify(userMapper, times(1)).executeUserProcedure(
                operationCaptor.capture(),
                isNull(),
                usernameCaptor.capture(),
                passwordCaptor.capture(),
                eq(true),
                authorityCaptor.capture()
        );

        assertEquals("C", operationCaptor.getValue());
        assertEquals(username, usernameCaptor.getValue());
        assertTrue(passwordCaptor.getValue().startsWith("{bcrypt}"));
        assertEquals(authority, authorityCaptor.getValue());
    }

    @Test
    @DisplayName("사용자 정보 수정 - 암호화된 비밀번호로 업데이트")
    void testUpdateUser_WithEncryptedPassword() {
        // given
        Long userId = 1L;
        String encodedPassword = "{bcrypt}$2a$10$newEncodedPassword";

        // when
        userService.updateUser(userId, null, encodedPassword, null, null);

        // then
        ArgumentCaptor<String> operationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);

        verify(userMapper, times(1)).executeUserProcedure(
                operationCaptor.capture(),
                idCaptor.capture(),
                isNull(),
                passwordCaptor.capture(),
                isNull(),
                isNull()
        );

        assertEquals("U", operationCaptor.getValue());
        assertEquals(userId, idCaptor.getValue());
        assertTrue(passwordCaptor.getValue().startsWith("{bcrypt}"));
    }

    @Test
    @DisplayName("사용자 삭제 - Soft Delete 처리")
    void testDeleteUser() {
        // given
        Long userId = 1L;

        // when
        userService.deleteUser(userId);

        // then
        ArgumentCaptor<String> operationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);

        verify(userMapper, times(1)).executeUserProcedure(
                operationCaptor.capture(),
                idCaptor.capture(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );

        assertEquals("D", operationCaptor.getValue());
        assertEquals(userId, idCaptor.getValue());
    }

    @Test
    @DisplayName("ID로 사용자 조회 성공 - 단건 조회")
    void testGetUserById_Success() {
        // given
        Long userId = 1L;
        when(userMapper.findUserById(userId)).thenReturn(testUser);

        // when
        User result = userService.getUserById(userId);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userMapper, times(1)).findUserById(userId);
    }

    @Test
    @DisplayName("ID로 사용자 조회 실패 - 사용자 없음")
    void testGetUserById_NotFound() {
        // given
        Long userId = 999L;
        when(userMapper.findUserById(userId)).thenReturn(null);

        // when
        User result = userService.getUserById(userId);

        // then
        assertNull(result);
        verify(userMapper, times(1)).findUserById(userId);
    }

    @Test
    @DisplayName("사용자명으로 사용자 조회 성공 - 단건 조회")
    void testGetUserByUsername_Success() {
        // given
        String username = "testuser";
        when(userMapper.findUserByIdOrUsername(null, username)).thenReturn(testUser);

        // when
        User result = userService.getUserByUsername(username);

        // then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper, times(1)).findUserByIdOrUsername(null, username);
    }

    @Test
    @DisplayName("전체 사용자 목록 조회 - 뷰를 통한 복수건 조회")
    void testGetUserList() {
        // given
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", 1L);
        user1.put("username", "user1");
        user1.put("authorities", "ROLE_USER");

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", 2L);
        user2.put("username", "user2");
        user2.put("authorities", "ROLE_ADMIN,ROLE_USER");

        when(userMapper.findAllUsersFromView()).thenReturn(Arrays.asList(user1, user2));

        // when
        List<Map<String, Object>> result = userService.getUserList();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).get("username"));
        assertEquals("user2", result.get(1).get("username"));
        verify(userMapper, times(1)).findAllUsersFromView();
    }

    @Test
    @DisplayName("사용자 검색 - 뷰를 통한 복수건 조회")
    void testSearchUsers() {
        // given
        String keyword = "test";
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1L);
        user.put("username", "testuser");
        user.put("authorities", "ROLE_USER");

        when(userMapper.searchUsersFromView(keyword)).thenReturn(Arrays.asList(user));

        // when
        List<Map<String, Object>> result = userService.searchUsers(keyword);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).get("username"));
        verify(userMapper, times(1)).searchUsersFromView(keyword);
    }
}
