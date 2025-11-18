package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.LoginRequest;
import com.sleekydz86.toaspayment.application.dto.LoginResponse;
import com.sleekydz86.toaspayment.domain.user.PasswordEncoder;
import com.sleekydz86.toaspayment.domain.user.User;
import com.sleekydz86.toaspayment.domain.user.UserRepository;
import com.sleekydz86.toaspayment.global.exception.BadRequestException;
import com.sleekydz86.toaspayment.infrastructure.security.JwtTokenProvider;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("로그인 Use Case 테스트")
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private LoginUseCase loginUseCase;

    private User testUser;
    private String testEmail;
    private String testPassword;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testPassword = "password123";
        encodedPassword = "$2a$10$encodedPasswordHash";

        testUser = User.create(testEmail, encodedPassword, "테스트 사용자");
    }

    @Test
    @DisplayName("정상적인 로그인 성공")
    void loginSuccess() {

        LoginRequest request = new LoginRequest(testEmail, testPassword);
        String expectedToken = "test-jwt-token";

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testPassword, encodedPassword)).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(), anyString())).thenReturn(expectedToken);

        LoginResponse response = loginUseCase.execute(request);

        assertThat(response.message()).isEqualTo("로그인 성공");
        assertThat(response.data().email()).isEqualTo(testEmail);
        assertThat(response.data().token()).isEqualTo(expectedToken);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시도")
    void loginWithNonExistentEmail() {

        LoginRequest request = new LoginRequest("nonexistent@example.com", testPassword);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.execute(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시도")
    void loginWithWrongPassword() {

        LoginRequest request = new LoginRequest(testEmail, "wrongPassword");
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", encodedPassword)).thenReturn(false);

        assertThatThrownBy(() -> loginUseCase.execute(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}
