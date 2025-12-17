package com.sleekydz86.sever.integration;

import com.sleekydz86.sever.model.application.service.UserService;
import com.sleekydz86.sever.model.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("User 통합 테스트")
class UserIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 - 비밀번호 암호화 검증")
    void testRegister_WithPasswordEncryption() {
        // given
        String username = "testuser" + System.currentTimeMillis();
        String rawPassword = "testpassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // when
        userService.register(username, encodedPassword, "ROLE_USER");

        // then
        assertTrue(encodedPassword.startsWith("{bcrypt}"));
        assertNotEquals(rawPassword, encodedPassword);

        User savedUser = userService.getUserByUsername(username);
        assertNotNull(savedUser);
        assertTrue(savedUser.getPassword().startsWith("{bcrypt}"));
        assertTrue(passwordEncoder.matches(rawPassword, savedUser.getPassword()));
    }

    @Test
    @DisplayName("사용자 정보 수정 - 비밀번호 암호화 검증")
    void testUpdateUser_WithPasswordEncryption() {
        // given
        String username = "updateuser" + System.currentTimeMillis();
        String initialPassword = "initial123";
        String encodedInitialPassword = passwordEncoder.encode(initialPassword);

        userService.register(username, encodedInitialPassword, "ROLE_USER");
        User user = userService.getUserByUsername(username);

        String newPassword = "newpassword456";
        String encodedNewPassword = passwordEncoder.encode(newPassword);

        // when
        userService.updateUser(user.getId(), null, encodedNewPassword, null, null);

        // then
        assertTrue(encodedNewPassword.startsWith("{bcrypt}"));
        assertNotEquals(newPassword, encodedNewPassword);

        User updatedUser = userService.getUserByUsername(username);
        assertNotNull(updatedUser);
        assertTrue(updatedUser.getPassword().startsWith("{bcrypt}"));
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
        assertFalse(passwordEncoder.matches(initialPassword, updatedUser.getPassword()));
    }

    @Test
    @DisplayName("비밀번호 암호화 - 동일 비밀번호도 다른 해시 생성")
    void testPasswordEncryption_AlwaysDifferent() {
        // given
        String password = "samepassword";

        // when
        String encoded1 = passwordEncoder.encode(password);
        String encoded2 = passwordEncoder.encode(password);

        // then
        assertNotEquals(encoded1, encoded2);
        assertTrue(passwordEncoder.matches(password, encoded1));
        assertTrue(passwordEncoder.matches(password, encoded2));
    }

    @Test
    @DisplayName("전체 사용자 목록 조회 - 뷰를 통한 조회")
    void testGetUserList() {
        // given
        String username = "listuser" + System.currentTimeMillis();
        String encodedPassword = passwordEncoder.encode("password123");
        userService.register(username, encodedPassword, "ROLE_USER");

        // when
        List<Map<String, Object>> users = userService.getUserList();

        // then
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    @DisplayName("사용자 검색 - 뷰를 통한 검색")
    void testSearchUsers() {
        // given
        String username = "searchuser" + System.currentTimeMillis();
        String encodedPassword = passwordEncoder.encode("password123");
        userService.register(username, encodedPassword, "ROLE_USER");

        // when
        List<Map<String, Object>> results = userService.searchUsers(username);

        // then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(username, results.get(0).get("username"));
    }

    @Test
    @DisplayName("사용자 삭제 - Soft Delete 처리")
    void testDeleteUser_SoftDelete() {
        // given
        String username = "deleteuser" + System.currentTimeMillis();
        String encodedPassword = passwordEncoder.encode("password123");
        userService.register(username, encodedPassword, "ROLE_USER");

        User user = userService.getUserByUsername(username);
        assertNotNull(user);
        assertTrue(user.isEnabled());

        // when
        userService.deleteUser(user.getId());

        // then
        User deletedUser = userService.getUserByUsername(username);
        assertNotNull(deletedUser);
        assertFalse(deletedUser.isEnabled());
    }

    @Test
    @DisplayName("단건 조회 - ID로 사용자 조회")
    void testGetUserById() {
        // given
        String username = "iduser" + System.currentTimeMillis();
        String encodedPassword = passwordEncoder.encode("password123");
        userService.register(username, encodedPassword, "ROLE_USER");

        User registeredUser = userService.getUserByUsername(username);
        Long userId = registeredUser.getId();

        // when
        User foundUser = userService.getUserById(userId);

        // then
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
        assertEquals(username, foundUser.getUsername());
    }
}
