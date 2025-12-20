package com.sleekydz86.passykey.application.usecase;

import com.sleekydz86.passykey.application.dto.RegisterRequest;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.port.inbound.UserUseCase;
import com.sleekydz86.passykey.domain.port.outbound.PasswordEncoderPort;
import com.sleekydz86.passykey.domain.port.outbound.UserRepositoryPort;
import com.sleekydz86.passykey.global.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.UUID;

@Service
public class UserUseCaseImpl implements UserUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UserUseCaseImpl.class);

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public UserUseCaseImpl(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(RegisterRequest request) {
        try {
            validateRegistrationRequest(request);

            String userHandle = generateUserHandle();
            String encodedPassword = passwordEncoder.encode(request.getPassword());

            User user = new User(
                    request.getUsername(),
                    encodedPassword,
                    request.getEmail(),
                    request.getDisplayName(),
                    userHandle
            );

            User savedUser = userRepository.save(user);
            logger.info("사용자 등록 성공: {}", savedUser.getUsername());
            return savedUser;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("사용자 등록 실패: {}", request.getUsername(), e);
            throw new RuntimeException("사용자 등록 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUserHandle(String userHandle) {
        return userRepository.findByUserHandle(userHandle)
                .orElseThrow(() -> new UserNotFoundException("핸들로 사용자를 찾을 수 없습니다: " + userHandle));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + request.getEmail());
        }
    }

    private String generateUserHandle() {
        byte[] bytes = UUID.randomUUID().toString().getBytes();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
