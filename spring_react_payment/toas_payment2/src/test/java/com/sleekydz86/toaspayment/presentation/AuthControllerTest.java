package com.sleekydz86.toaspayment.presentation;

import com.sleekydz86.toaspayment.application.dto.LoginRequest;
import com.sleekydz86.toaspayment.application.dto.LoginResponse;
import com.sleekydz86.toaspayment.application.dto.RegisterRequest;
import com.sleekydz86.toaspayment.application.dto.RegisterResponse;
import com.sleekydz86.toaspayment.application.usecase.LoginUseCase;
import com.sleekydz86.toaspayment.application.usecase.RegisterUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("인증 Controller 테스트")
class AuthControllerTest {

    @Mock
    private LoginUseCase loginUseCase;

    @Mock
    private RegisterUseCase registerUseCase;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        LoginResponse expectedResponse = new LoginResponse(
                "로그인 성공",
                new LoginResponse.LoginData(1L, "test@example.com", "테스트 사용자", "jwt_token"));

        when(loginUseCase.execute(request)).thenReturn(expectedResponse);

        // when
        ResponseEntity<LoginResponse> response = authController.login(request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("로그인 성공");
        assertThat(response.getBody().data().email()).isEqualTo("test@example.com");
        assertThat(response.getBody().data().token()).isEqualTo("jwt_token");
    }

    @Test
    @DisplayName("회원가입 성공")
    void register_success() {
        // given
        RegisterRequest request = new RegisterRequest(
                "newuser@example.com",
                "password123",
                "새로운 사용자");
        RegisterResponse expectedResponse = new RegisterResponse(
                "회원가입 성공",
                new RegisterResponse.RegisterData(1L, "newuser@example.com", "새로운 사용자"));

        when(registerUseCase.execute(request)).thenReturn(expectedResponse);

        // when
        ResponseEntity<RegisterResponse> response = authController.register(request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("회원가입 성공");
        assertThat(response.getBody().data().email()).isEqualTo("newuser@example.com");
    }
}

