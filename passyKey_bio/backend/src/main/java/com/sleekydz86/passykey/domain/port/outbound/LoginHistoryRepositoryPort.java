package com.sleekydz86.passykey.domain.port.outbound;

import com.sleekydz86.passykey.domain.model.LoginHistory;
import com.sleekydz86.passykey.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface LoginHistoryRepositoryPort {
    LoginHistory save(LoginHistory loginHistory);
    Optional<LoginHistory> findActiveSessionByUserId(Long userId);
    Optional<LoginHistory> findBySessionId(String sessionId);
    List<LoginHistory> findByUserOrderByLoginAtDesc(User user, int limit);
    void updateLogoutAt(Long id, java.time.LocalDateTime logoutAt);
    List<LoginHistory> findAllActiveSessions();
}

