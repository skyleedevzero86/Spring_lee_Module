package com.sleekydz86.passykey.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User 도메인 모델 테스트")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(
                "testuser",
                "encodedPassword",
                "test@example.com",
                "Test User",
                "userHandle123"
        );
        user.setId(1L);
    }

    @Test
    @DisplayName("User 생성 및 기본 정보 확인")
    void createUser_Success() {
        // then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getDisplayName()).isEqualTo("Test User");
        assertThat(user.getUserHandle()).isEqualTo("userHandle123");
    }

    @Test
    @DisplayName("UserDetails 인터페이스 구현 확인")
    void userDetails_Implementation() {
        // when
        Collection<?> authorities = user.getAuthorities();
        String username = user.getUsername();
        String password = user.getPassword();
        boolean accountNonExpired = user.isAccountNonExpired();
        boolean accountNonLocked = user.isAccountNonLocked();
        boolean credentialsNonExpired = user.isCredentialsNonExpired();
        boolean enabled = user.isEnabled();

        // then
        assertThat(authorities).isNotNull();
        assertThat(username).isEqualTo("testuser");
        assertThat(password).isEqualTo("encodedPassword");
        assertThat(accountNonExpired).isTrue();
        assertThat(accountNonLocked).isTrue();
        assertThat(credentialsNonExpired).isTrue();
        assertThat(enabled).isTrue();
    }

    @Test
    @DisplayName("User 필드 수정")
    void updateUserFields_Success() {
        // when
        user.setUsername("newuser");
        user.setEmail("new@example.com");
        user.setDisplayName("New User");
        user.setUserHandle("newHandle");

        // then
        assertThat(user.getUsername()).isEqualTo("newuser");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getDisplayName()).isEqualTo("New User");
        assertThat(user.getUserHandle()).isEqualTo("newHandle");
    }

    @Test
    @DisplayName("User 상태 변경")
    void updateUserStatus_Success() {
        // when
        user.setEnabled(false);
        user.setAccountNonExpired(false);
        user.setAccountNonLocked(false);
        user.setCredentialsNonExpired(false);

        // then
        assertThat(user.isEnabled()).isFalse();
        assertThat(user.isAccountNonExpired()).isFalse();
        assertThat(user.isAccountNonLocked()).isFalse();
        assertThat(user.isCredentialsNonExpired()).isFalse();
    }

    @Test
    @DisplayName("User에 인증서 목록 설정")
    void setCredentials_Success() {
        // given
        WebAuthnCredential credential1 = new WebAuthnCredential(
                "credentialId1",
                "publicKeyCose1",
                0L,
                "usb",
                user
        );
        WebAuthnCredential credential2 = new WebAuthnCredential(
                "credentialId2",
                "publicKeyCose2",
                0L,
                "nfc",
                user
        );

        // when
        user.setCredentials(java.util.Arrays.asList(credential1, credential2));

        // then
        assertThat(user.getCredentials()).isNotNull();
        assertThat(user.getCredentials()).hasSize(2);
        assertThat(user.getCredentials().get(0).getCredentialId()).isEqualTo("credentialId1");
        assertThat(user.getCredentials().get(1).getCredentialId()).isEqualTo("credentialId2");
    }
}


