package com.sleekydz86.passykey.adapter.outbound.persistence;

import com.sleekydz86.passykey.adapter.outbound.service.UserCacheService;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.port.outbound.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryAdapter.class);

    private final UserMyBatisMapper userMapper;
    private final UserCacheService userCacheService;

    public UserRepositoryAdapter(UserMyBatisMapper userMapper, UserCacheService userCacheService) {
        this.userMapper = userMapper;
        this.userCacheService = userCacheService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User save(User user) {
        try {
            String operation = (user.getId() == null) ? "C" : "U";
            Map<String, Object> resultId = new HashMap<>();
            
            userMapper.save(operation, user, resultId);
            
            if (user.getId() == null && resultId.get("resultId") != null) {
                user.setId(((Number) resultId.get("resultId")).longValue());
            }
            
            userCacheService.cacheUser(user.getUsername(), user);
            return user;
        } catch (DuplicateKeyException e) {
            logger.error("사용자 저장 중 중복 키 오류 발생: {}", user.getUsername(), e);
            throw new IllegalArgumentException("이미 존재하는 사용자명 또는 이메일입니다", e);
        } catch (DataAccessException e) {
            logger.error("사용자 저장 중 데이터베이스 오류 발생: {}", user.getUsername(), e);
            throw new RuntimeException("사용자 저장 실패", e);
        } catch (Exception e) {
            logger.error("사용자 저장 중 예상치 못한 오류 발생: {}", user.getUsername(), e);
            throw new RuntimeException("사용자 저장 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        try {
            User cachedUser = userCacheService.getUserFromCache(username);
            if (cachedUser != null) {
                return Optional.of(cachedUser);
            }
            User user = userMapper.selectByUsername(username);
            if (user != null) {
                userCacheService.cacheUser(username, user);
            }
            return Optional.ofNullable(user);
        } catch (DataAccessException e) {
            logger.error("사용자 조회 중 데이터베이스 오류 발생: {}", username, e);
            throw new RuntimeException("사용자 조회 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        try {
            User user = userMapper.selectByEmail(email);
            return Optional.ofNullable(user);
        } catch (DataAccessException e) {
            logger.error("이메일로 사용자 조회 중 데이터베이스 오류 발생: {}", email, e);
            throw new RuntimeException("사용자 조회 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUserHandle(String userHandle) {
        try {
            User user = userMapper.selectByUserHandle(userHandle);
            return Optional.ofNullable(user);
        } catch (DataAccessException e) {
            logger.error("사용자 핸들로 조회 중 데이터베이스 오류 발생: {}", userHandle, e);
            throw new RuntimeException("사용자 조회 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        try {
            return userMapper.existsByUsername(username);
        } catch (DataAccessException e) {
            logger.error("사용자명 존재 확인 중 데이터베이스 오류 발생: {}", username, e);
            throw new RuntimeException("사용자명 확인 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        try {
            return userMapper.existsByEmail(email);
        } catch (DataAccessException e) {
            logger.error("이메일 존재 확인 중 데이터베이스 오류 발생: {}", email, e);
            throw new RuntimeException("이메일 확인 실패", e);
        }
    }
}
