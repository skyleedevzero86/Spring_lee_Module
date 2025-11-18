package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.RegisterRequest;
import com.sleekydz86.toaspayment.application.dto.RegisterResponse;
import com.sleekydz86.toaspayment.domain.user.PasswordEncoder;
import com.sleekydz86.toaspayment.domain.user.User;
import com.sleekydz86.toaspayment.domain.user.UserRepository;
import com.sleekydz86.toaspayment.global.exception.BadRequestException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("회원가입 Use Case 테스트")
class RegisterUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUseCase registerUseCase;

    private RegisterRequest request;
    private String testEmail;
    private String testPassword;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        testEmail = "newuser@example.com";
        testPassword = "password123";
        encodedPassword = "$2a$10$encodedPasswordHash";
        request = new RegisterRequest(testEmail, testPassword, "새 사용자");
    }

    @Test
    @DisplayName("정상적인 회원가입 성공")
    void registerSuccess() {

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = registerUseCase.execute(request);

        assertThat(response.message()).isEqualTo("회원가입이 완료되었습니다.");
        assertThat(response.data().email()).isEqualTo(testEmail);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(testEmail);
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 시도")
    void registerWithExistingEmail() {

        User existingUser = User.create(testEmail, encodedPassword, "기존 사용자");
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> registerUseCase.execute(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("이미 사용 중인 이메일입니다.");
    }
}
