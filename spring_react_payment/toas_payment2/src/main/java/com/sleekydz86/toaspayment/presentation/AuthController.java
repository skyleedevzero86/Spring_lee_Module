package com.sleekydz86.toaspayment.presentation;

import com.sleekydz86.toaspayment.application.dto.LoginRequest;
import com.sleekydz86.toaspayment.application.dto.LoginResponse;
import com.sleekydz86.toaspayment.application.dto.RegisterRequest;
import com.sleekydz86.toaspayment.application.dto.RegisterResponse;
import com.sleekydz86.toaspayment.application.usecase.LoginUseCase;
import com.sleekydz86.toaspayment.application.usecase.RegisterUseCase;
import com.sleekydz86.toaspayment.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "사용자 인증 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AuthController {
    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = registerUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성을 검증합니다.")
    @GetMapping("/validate-token")
    public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authorization.substring(7);
        if (jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(401).build();
    }
}
