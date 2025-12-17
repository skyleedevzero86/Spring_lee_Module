package com.sleekydz86.sever.global.config;

import com.sleekydz86.sever.model.infrastructure.persistence.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

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
            userMapper.executeUserProcedure("U", adminUserId, "admin", encodedPassword, true, null);
            
            List<String> existingAuthorities = userMapper.findAuthoritiesByUsername("admin");
            boolean hasAdminRole = existingAuthorities != null && existingAuthorities.contains("ROLE_ADMIN");
            boolean hasUserRole = existingAuthorities != null && existingAuthorities.contains("ROLE_USER");
            
            if (!hasAdminRole) {
                userMapper.insertAuthority(adminUserId, "ROLE_ADMIN");
            }
            if (!hasUserRole) {
                userMapper.insertAuthority(adminUserId, "ROLE_USER");
            }
        }
    }
}
