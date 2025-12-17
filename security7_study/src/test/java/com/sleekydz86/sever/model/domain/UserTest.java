package com.sleekydz86.sever.model.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User 도메인 모델 테스트")
class UserTest {

    @Test
    @DisplayName("사용자 생성 - 모든 필드 포함")
    void testUserCreation() {
        // given
        List<String> authorities = Arrays.asList("ROLE_USER");

        // when
        User user = new User(1L, "testuser", "password", true, authorities);

        // then
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password", user.getPassword());
        assertTrue(user.isEnabled());
        assertEquals(1, user.getAuthorities().size());
        assertEquals("ROLE_USER", user.getAuthorities().get(0));
    }

    @Test
    @DisplayName("사용자 생성 - Setter를 통한 필드 설정")
    void testUserSetters() {
        // given
        User user = new User();

        // when
        user.setId(2L);
        user.setUsername("admin");
        user.setPassword("admin123");
        user.setEnabled(true);
        user.setAuthorities(Arrays.asList("ROLE_ADMIN", "ROLE_USER"));

        // then
        assertEquals(2L, user.getId());
        assertEquals("admin", user.getUsername());
        assertEquals("admin123", user.getPassword());
        assertTrue(user.isEnabled());
        assertEquals(2, user.getAuthorities().size());
    }

    @Test
    @DisplayName("사용자 생성 - 다중 권한 포함")
    void testUserWithMultipleAuthorities() {
        // given
        List<String> authorities = Arrays.asList("ROLE_ADMIN", "ROLE_USER");

        // when
        User user = new User(3L, "admin", "admin", true, authorities);

        // then
        assertEquals(2, user.getAuthorities().size());
        assertTrue(user.getAuthorities().contains("ROLE_ADMIN"));
        assertTrue(user.getAuthorities().contains("ROLE_USER"));
    }

    @Test
    @DisplayName("사용자 생성 - 비활성화 상태")
    void testUserDisabled() {
        // given
        // when
        User user = new User(4L, "disabled", "pass", false, Arrays.asList("ROLE_USER"));

        // then
        assertFalse(user.isEnabled());
    }
}
