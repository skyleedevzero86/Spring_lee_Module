package com.sleekydz86.sever.global.security;

import com.sleekydz86.sever.model.application.service.UserService;
import com.sleekydz86.sever.model.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService 테스트")
class CustomUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "{bcrypt}$2a$10$encoded", true, Arrays.asList("ROLE_USER"));
    }

    @Test
    @DisplayName("사용자명으로 UserDetails 로드 성공 - 암호화된 비밀번호 포함")
    void testLoadUserByUsername_Success() {
        // given
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("{bcrypt}$2a$10$encoded", userDetails.getPassword());
        assertTrue(userDetails.getPassword().startsWith("{bcrypt}"));
        assertTrue(userDetails.isEnabled());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
        verify(userService, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("사용자명으로 UserDetails 로드 - 다중 권한")
    void testLoadUserByUsername_WithMultipleAuthorities() {
        // given
        User adminUser = new User(2L, "admin", "{bcrypt}$2a$10$adminPassword", true,
                Arrays.asList("ROLE_ADMIN", "ROLE_USER"));
        when(userService.findByUsername("admin")).thenReturn(adminUser);

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin");

        // then
        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
        assertTrue(userDetails.getPassword().startsWith("{bcrypt}"));
        assertEquals(2, userDetails.getAuthorities().size());
        verify(userService, times(1)).findByUsername("admin");
    }

    @Test
    @DisplayName("사용자명으로 UserDetails 로드 실패 - 사용자 없음")
    void testLoadUserByUsername_UserNotFound() {
        // given
        when(userService.findByUsername("nonexistent")).thenReturn(null);

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistent");
        });

        verify(userService, times(1)).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("사용자명으로 UserDetails 로드 - 비활성화된 사용자")
    void testLoadUserByUsername_DisabledUser() {
        // given
        User disabledUser = new User(3L, "disabled", "{bcrypt}$2a$10$password", false, Arrays.asList("ROLE_USER"));
        when(userService.findByUsername("disabled")).thenReturn(disabledUser);

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("disabled");

        // then
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
        assertTrue(userDetails.getPassword().startsWith("{bcrypt}"));
        verify(userService, times(1)).findByUsername("disabled");
    }

    @Test
    @DisplayName("로그인 시 암호화된 비밀번호 반환 검증 - DB에서 가져온 암호화 비밀번호를 그대로 반환")
    void testLoadUserByUsername_EncryptedPasswordFromDatabase() {
        // given
        String encryptedPasswordFromDB = "{bcrypt}$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6LW";
        User userWithEncryptedPassword = new User(4L, "loginuser", encryptedPasswordFromDB, true,
                Arrays.asList("ROLE_USER"));
        when(userService.findByUsername("loginuser")).thenReturn(userWithEncryptedPassword);

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("loginuser");

        // then
        assertNotNull(userDetails);
        assertEquals(encryptedPasswordFromDB, userDetails.getPassword());
        assertTrue(userDetails.getPassword().startsWith("{bcrypt}"));
        verify(userService, times(1)).findByUsername("loginuser");
    }
}
