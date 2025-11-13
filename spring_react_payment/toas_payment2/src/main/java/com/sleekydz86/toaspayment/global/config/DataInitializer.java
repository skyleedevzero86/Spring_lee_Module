package com.sleekydz86.toaspayment.global.config;

import com.sleekydz86.toaspayment.domain.user.PasswordEncoder;
import com.sleekydz86.toaspayment.domain.user.User;
import com.sleekydz86.toaspayment.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("hong@example.com").isEmpty()) {
            String encodedPassword = passwordEncoder.encode("password123");
            User user = User.create("hong@example.com", encodedPassword, "홍길동");
            userRepository.save(user);
            log.info("테스트 사용자 생성 완료 - 이메일: {}", user.getEmail());
        }
    }
}
