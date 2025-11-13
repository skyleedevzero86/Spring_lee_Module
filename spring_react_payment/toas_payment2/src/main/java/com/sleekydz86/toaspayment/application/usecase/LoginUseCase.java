package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.LoginRequest;
import com.sleekydz86.toaspayment.application.dto.LoginResponse;
import com.sleekydz86.toaspayment.domain.user.PasswordEncoder;
import com.sleekydz86.toaspayment.domain.user.User;
import com.sleekydz86.toaspayment.domain.user.UserRepository;
import com.sleekydz86.toaspayment.global.exception.BadRequestException;
import com.sleekydz86.toaspayment.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse execute(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (!user.matchesPassword(request.password(), passwordEncoder)) {
            throw new BadRequestException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        log.info("로그인 성공 - 사용자 ID: {}, 이메일: {}, 역할: {}", user.getId(), user.getEmail(), user.getRole());

        return new LoginResponse(
                "로그인 성공",
                new LoginResponse.LoginData(user.getId(), user.getEmail(), user.getName(), user.getRole().name(),
                        token));
    }
}
