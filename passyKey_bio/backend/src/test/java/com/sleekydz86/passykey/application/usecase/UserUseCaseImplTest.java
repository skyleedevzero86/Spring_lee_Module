package com.sleekydz86.passykey.application.usecase;

import com.sleekydz86.passykey.application.dto.RegisterRequest;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.port.outbound.PasswordEncoderPort;
import com.sleekydz86.passykey.domain.port.outbound.UserRepositoryPort;
import com.sleekydz86.passykey.global.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserUseCaseImpl 테스트")
class UserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private UserUseCaseImpl userUseCase;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "testuser",
                "password123",
                "test@example.com",
                "Test User"
        );

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
    @DisplayName("사용자 등록 성공")
    void register_Success() {
        // given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        User result = userUseCase.register(registerRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 등록 실패 - 이미 존재하는 사용자명")
    void register_Fail_DuplicateUsername() {
        // given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userUseCase.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 사용자명입니다");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 등록 실패 - 이미 존재하는 이메일")
    void register_Fail_DuplicateEmail() {
        // given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userUseCase.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 이메일입니다");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자명으로 사용자 조회 성공")
    void findByUsername_Success() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // when
        User result = userUseCase.findByUsername("testuser");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("사용자명으로 사용자 조회 실패 - 사용자 없음")
    void findByUsername_Fail_UserNotFound() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userUseCase.findByUsername("testuser"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 핸들로 사용자 조회 성공")
    void findByUserHandle_Success() {
        // given
        when(userRepository.findByUserHandle("userHandle123")).thenReturn(Optional.of(user));

        // when
        User result = userUseCase.findByUserHandle("userHandle123");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserHandle()).isEqualTo("userHandle123");
    }

    @Test
    @DisplayName("사용자 핸들로 사용자 조회 실패 - 사용자 없음")
    void findByUserHandle_Fail_UserNotFound() {
        // given
        when(userRepository.findByUserHandle("userHandle123")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userUseCase.findByUserHandle("userHandle123"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자명 존재 확인 - 존재함")
    void existsByUsername_Exists() {
        // given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // when
        boolean result = userUseCase.existsByUsername("testuser");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("사용자명 존재 확인 - 존재하지 않음")
    void existsByUsername_NotExists() {
        // given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);

        // when
        boolean result = userUseCase.existsByUsername("testuser");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("이메일 존재 확인 - 존재함")
    void existsByEmail_Exists() {
        // given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // when
        boolean result = userUseCase.existsByEmail("test@example.com");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이메일 존재 확인 - 존재하지 않음")
    void existsByEmail_NotExists() {
        // given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        // when
        boolean result = userUseCase.existsByEmail("test@example.com");

        // then
        assertThat(result).isFalse();
    }
}

