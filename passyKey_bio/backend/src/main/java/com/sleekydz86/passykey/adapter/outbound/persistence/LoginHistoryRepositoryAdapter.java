package com.sleekydz86.passykey.adapter.outbound.persistence;

import com.sleekydz86.passykey.domain.model.LoginHistory;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.port.outbound.LoginHistoryRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class LoginHistoryRepositoryAdapter implements LoginHistoryRepositoryPort {

    private static final Logger logger = LoggerFactory.getLogger(LoginHistoryRepositoryAdapter.class);

    private final LoginHistoryMyBatisMapper loginHistoryMapper;

    public LoginHistoryRepositoryAdapter(LoginHistoryMyBatisMapper loginHistoryMapper) {
        this.loginHistoryMapper = loginHistoryMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginHistory save(LoginHistory loginHistory) {
        try {
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("operation", "C");
            params.put("id", null);
            params.put("userId", loginHistory.getUserId());
            params.put("loginType", loginHistory.getLoginType());
            params.put("sessionId", loginHistory.getSessionId());
            params.put("ipAddress", loginHistory.getIpAddress());
            params.put("userAgent", loginHistory.getUserAgent());
            params.put("loginAt", loginHistory.getLoginAt());
            params.put("logoutAt", null);
            params.put("resultId", null);

            loginHistoryMapper.insert(params);

            Long resultId = (Long) params.get("resultId");
            if (resultId != null) {
                loginHistory.setId(resultId);
            }

            logger.debug("로그인 이력 저장 완료: userId={}, sessionId={}, loginType={}, id={}",
                    loginHistory.getUserId(), loginHistory.getSessionId(), loginHistory.getLoginType(), resultId);
            return loginHistory;
        } catch (Exception e) {
            logger.error("로그인 이력 저장 실패: userId={}, sessionId={}",
                    loginHistory.getUserId(), loginHistory.getSessionId(), e);
            throw new RuntimeException("로그인 이력 저장 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LoginHistory> findActiveSessionByUserId(Long userId) {
        try {
            LoginHistory history = loginHistoryMapper.selectActiveSessionByUserId(userId);
            return Optional.ofNullable(history);
        } catch (Exception e) {
            logger.error("활성 세션 조회 실패: userId={}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LoginHistory> findBySessionId(String sessionId) {
        try {
            LoginHistory history = loginHistoryMapper.selectBySessionId(sessionId);
            return Optional.ofNullable(history);
        } catch (Exception e) {
            logger.error("세션 ID로 로그인 이력 조회 실패: sessionId={}", sessionId, e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoginHistory> findByUserOrderByLoginAtDesc(User user, int limit) {
        try {
            return loginHistoryMapper.selectByUserIdOrderByLoginAtDesc(user.getId(), limit);
        } catch (Exception e) {
            logger.error("로그인 이력 조회 실패: userId={}", user.getId(), e);
            throw new RuntimeException("로그인 이력 조회 실패", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLogoutAt(Long id, java.time.LocalDateTime logoutAt) {
        try {
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("operation", "U");
            params.put("id", id);
            params.put("userId", null);
            params.put("loginType", null);
            params.put("sessionId", null);
            params.put("ipAddress", null);
            params.put("userAgent", null);
            params.put("loginAt", null);
            params.put("logoutAt", logoutAt);
            params.put("resultId", null);

            loginHistoryMapper.updateLogoutAt(params);

            Long resultId = (Long) params.get("resultId");
            if (resultId == null || resultId == 0) {
                logger.warn("로그아웃 시간 업데이트 실패: id={}, resultId={}", id, resultId);
            } else {
                logger.debug("로그아웃 시간 업데이트 완료: id={}, resultId={}", id, resultId);
            }
        } catch (Exception e) {
            logger.error("로그아웃 시간 업데이트 실패: id={}", id, e);
            throw new RuntimeException("로그아웃 시간 업데이트 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoginHistory> findAllActiveSessions() {
        try {
            return loginHistoryMapper.selectAllActiveSessions();
        } catch (Exception e) {
            logger.error("모든 활성 세션 조회 실패", e);
            return java.util.Collections.emptyList();
        }
    }
}
