package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.RegisterRequest;
import com.sleekydz86.toaspayment.application.dto.RegisterResponse;
import com.sleekydz86.toaspayment.domain.user.PasswordEncoder;
import com.sleekydz86.toaspayment.domain.user.User;
import com.sleekydz86.toaspayment.domain.user.UserRepository;
import com.sleekydz86.toaspayment.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse execute(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BadRequestException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.create(request.email(), encodedPassword, request.name());
        userRepository.save(user);

        log.info("회원가입 완료 - 사용자 ID: {}, 이메일: {}", user.getId(), user.getEmail());

        return new RegisterResponse(
                "회원가입이 완료되었습니다.",
                new RegisterResponse.RegisterData(user.getId(), user.getEmail(), user.getName())
        );
    }
}


