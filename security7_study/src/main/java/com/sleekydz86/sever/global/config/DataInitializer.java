package com.sleekydz86.sever.global.config;

import com.sleekydz86.sever.model.infrastructure.persistence.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userMapper.findByUsername("user") == null) {
            String encodedPassword = passwordEncoder.encode("password");
            userMapper.executeUserProcedure("C", null, "user", encodedPassword, true, "ROLE_USER");
        }
        if (userMapper.findByUsername("admin") == null) {
            String encodedPassword = passwordEncoder.encode("admin");
            userMapper.executeUserProcedure("C", null, "admin", encodedPassword, true, "ROLE_ADMIN");
            Long userId = userMapper.findUserIdByUsername("admin");
            if (userId != null) {
                userMapper.insertAuthority(userId, "ROLE_USER");
            }
        }
    }
}
