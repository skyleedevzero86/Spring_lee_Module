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
        
        Long adminUserId = userMapper.findUserIdByUsername("admin");
        if (adminUserId == null) {
            String encodedPassword = passwordEncoder.encode("admin");
            userMapper.executeUserProcedure("C", null, "admin", encodedPassword, true, "ROLE_ADMIN");
            adminUserId = userMapper.findUserIdByUsername("admin");
            if (adminUserId != null) {
                userMapper.insertAuthority(adminUserId, "ROLE_USER");
            }
        } else {
            String encodedPassword = passwordEncoder.encode("admin");
            userMapper.executeUserProcedure("U", adminUserId, "admin", encodedPassword, true, "ROLE_ADMIN");
            userMapper.insertAuthority(adminUserId, "ROLE_USER");
        }
    }
}
