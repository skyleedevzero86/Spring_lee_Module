package com.sleekydz86.toaspayment.domain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("User 도메인 테스트")
class UserTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private String email;
    private String encodedPassword;
    private String name;
    private String rawPassword;

    @BeforeEach
    void setUp() {
        email = "test@example.com";
        encodedPassword = "$2a$10$encodedPasswordHash";
        name = "테스트 사용자";
        rawPassword = "password123";
    }

    @Test
    @DisplayName("User 생성 성공")
    void createUser_success() {

        User user = User.create(email, encodedPassword, name);

        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(encodedPassword);
        assertThat(user.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("비밀번호 일치 확인 성공")
    void matchesPassword_success() {

        User user = User.create(email, encodedPassword, name);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        boolean result = user.matchesPassword(rawPassword, passwordEncoder);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("비밀번호 불일치 확인")
    void matchesPassword_fail() {

        User user = User.create(email, encodedPassword, name);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        boolean result = user.matchesPassword(rawPassword, passwordEncoder);

        assertThat(result).isFalse();
    }
}

