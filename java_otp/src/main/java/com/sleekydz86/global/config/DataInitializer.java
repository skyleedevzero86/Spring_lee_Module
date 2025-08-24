package com.sleekydz86.global.config;

import com.sleekydz86.domain.entity.User;
import com.sleekydz86.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {
            // 테스트용 사용자 생성
            if (!userRepository.existsByUsername("testuser")) {
                User testUser = User.builder()
                        .username("testuser")
                        .password(passwordEncoder.encode("password123"))
                        .otpEnabled(false)
                        .otpVerified(false)
                        .build();
                userRepository.save(testUser);

                System.out.println("테스트 사용자 생성 완료:");
                System.out.println("Username: testuser");
                System.out.println("Password: password123");
            }
        };
    }
}