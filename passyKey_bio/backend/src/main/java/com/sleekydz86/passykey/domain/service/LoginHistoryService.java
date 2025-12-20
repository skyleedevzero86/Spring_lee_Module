package com.sleekydz86.passykey.domain.service;

import com.sleekydz86.passykey.adapter.outbound.service.SessionCacheService;
import com.sleekydz86.passykey.domain.model.LoginHistory;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.port.outbound.LoginHistoryRepositoryPort;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoginHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(LoginHistoryService.class);

    private final LoginHistoryRepositoryPort loginHistoryRepository;
    private final SessionCacheService sessionCacheService;

    public LoginHistoryService(
            LoginHistoryRepositoryPort loginHistoryRepository,
            SessionCacheService sessionCacheService) {
        this.loginHistoryRepository = loginHistoryRepository;
        this.sessionCacheService = sessionCacheService;
    }

    public LoginHistory recordLogin(User user, String loginType, String sessionId, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        LoginHistory loginHistory = new LoginHistory(
            user.getId(),
            loginType,
            sessionId,
            ipAddress,
            userAgent != null ? (userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent) : null
        );
        
        LoginHistory saved = loginHistoryRepository.save(loginHistory);
        sessionCacheService.cacheSession(sessionId, user.getUsername());
        
        logger.info("로그인 이력 저장: userId={}, loginType={}, sessionId={}, ipAddress={}", 
            user.getId(), loginType, sessionId, ipAddress);
        
        return saved;
    }

    public boolean hasActiveSession(User user) {
        return loginHistoryRepository.findActiveSessionByUserId(user.getId()).isPresent();
    }
    
    public String getActiveSessionIdFromRedis(User user) {
        return sessionCacheService.getActiveSessionId(user.getUsername());
    }
    
    public String getActiveSessionId(User user) {
        String activeSessionId = sessionCacheService.getActiveSessionId(user.getUsername());
        if (activeSessionId != null) {
            return activeSessionId;
        }
        
        return loginHistoryRepository.findActiveSessionByUserId(user.getId())
            .map(LoginHistory::getSessionId)
            .orElse(null);
    }

    public void invalidatePreviousSession(User user) {
        loginHistoryRepository.findActiveSessionByUserId(user.getId())
            .ifPresent(history -> {
                loginHistoryRepository.updateLogoutAt(history.getId(), LocalDateTime.now());
                sessionCacheService.evictSession(history.getSessionId());
                logger.info("이전 세션 무효화: userId={}, sessionId={}", user.getId(), history.getSessionId());
            });
    }
    
    public boolean isSessionActive(String sessionId) {
        try {
            java.util.Optional<LoginHistory> historyOpt = loginHistoryRepository.findBySessionId(sessionId);
            if (historyOpt.isEmpty()) {
                logger.debug("세션 ID에 해당하는 로그인 이력이 없음: sessionId={}", sessionId);
                return false;
            }
            
            LoginHistory history = historyOpt.get();
            if (history.getLogoutAt() != null) {
                logger.debug("세션이 이미 로그아웃됨: sessionId={}, logoutAt={}", sessionId, history.getLogoutAt());
                return false;
            }
            
            String username = sessionCacheService.getUsernameFromSession(sessionId);
            if (username == null) {
                logger.debug("Redis에서 세션에 해당하는 사용자명을 찾을 수 없음: sessionId={}. 세션은 비활성으로 간주.", sessionId);
                return false;
            }
            
            String activeSessionId = sessionCacheService.getActiveSessionId(username);
            if (activeSessionId == null || !activeSessionId.equals(sessionId)) {
                logger.debug("Redis에서 활성 세션 ID와 현재 세션 ID가 일치하지 않음: sessionId={}, activeSessionId={}. 세션은 비활성으로 간주.", sessionId, activeSessionId);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.warn("세션 활성 상태 확인 중 오류 발생: sessionId={}. 세션은 비활성으로 간주.", sessionId, e);
            return false;
        }
    }

    public void recordLogout(String sessionId) {
        loginHistoryRepository.findBySessionId(sessionId)
            .ifPresent(history -> {
                loginHistoryRepository.updateLogoutAt(history.getId(), LocalDateTime.now());
                sessionCacheService.evictSession(sessionId);
                logger.info("로그아웃 이력 저장: sessionId={}", sessionId);
            });
    }

    public List<LoginHistory> getLoginHistory(User user, int limit) {
        return loginHistoryRepository.findByUserOrderByLoginAtDesc(user, limit);
    }

    @PostConstruct
    public void invalidateAllActiveSessionsOnStartup() {
        try {
            List<LoginHistory> activeSessions = loginHistoryRepository.findAllActiveSessions();
            if (activeSessions.isEmpty()) {
                logger.info("서버 시작 시 무효화할 활성 세션이 없습니다.");
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            int count = 0;
            for (LoginHistory history : activeSessions) {
                try {
                    loginHistoryRepository.updateLogoutAt(history.getId(), now);
                    sessionCacheService.evictSession(history.getSessionId());
                    count++;
                } catch (Exception e) {
                    logger.warn("서버 시작 시 세션 무효화 실패: sessionId={}", history.getSessionId(), e);
                }
            }

            logger.info("서버 시작 시 {}개의 활성 세션을 무효화했습니다.", count);
        } catch (Exception e) {
            logger.error("서버 시작 시 활성 세션 무효화 중 오류 발생", e);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        if (ip != null && (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1"))) {
            ip = getLocalNetworkIp();
        }
        
        return ip;
    }
    
    private String getLocalNetworkIp() {
        try {
            java.net.NetworkInterface networkInterface = java.util.Collections.list(
                java.net.NetworkInterface.getNetworkInterfaces()
            ).stream()
                .filter(ni -> {
                    try {
                        return !ni.isLoopback() && ni.isUp();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst()
                .orElse(null);
            
            if (networkInterface != null) {
                java.util.Enumeration<java.net.InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress address = addresses.nextElement();
                    if (address instanceof java.net.Inet4Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("로컬 네트워크 IP 조회 실패", e);
        }
        
        return "127.0.0.1";
    }
}

