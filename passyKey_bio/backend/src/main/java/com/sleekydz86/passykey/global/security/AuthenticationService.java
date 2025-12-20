package com.sleekydz86.passykey.global.security;

import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.service.LoginHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final LoginHistoryService loginHistoryService;

    public AuthenticationService(LoginHistoryService loginHistoryService) {
        this.loginHistoryService = loginHistoryService;
    }

    public void checkAndPreventDuplicateLogin(User user, HttpServletRequest request) {
        HttpSession currentSession = request.getSession(false);
        String currentSessionId = currentSession != null ? currentSession.getId() : null;

        if (currentSessionId == null) {
            return;
        }

        if (loginHistoryService.hasActiveSession(user)) {
            String activeSessionIdFromRedis = loginHistoryService.getActiveSessionIdFromRedis(user);

            if (activeSessionIdFromRedis == null) {
                loginHistoryService.invalidatePreviousSession(user);
                return;
            }

            if (activeSessionIdFromRedis.equals(currentSessionId)) {
                return;
            }

            boolean isRedisSessionValid = loginHistoryService.isSessionActive(activeSessionIdFromRedis);
            if (!isRedisSessionValid) {
                loginHistoryService.invalidatePreviousSession(user);
                return;
            }

            throw new IllegalStateException("이미 다른 세션에서 로그인되어 있습니다. 중복 로그인은 허용되지 않습니다.");
        }
    }

    public void setAuthentication(User user, HttpServletRequest request, String loginType) {
        loginHistoryService.invalidatePreviousSession(user);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetails(request));

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        HttpSession session = request.getSession(true);
        String newSessionId = session.getId();

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        loginHistoryService.recordLogin(user, loginType, newSessionId, request);
    }

    public void clearAuthentication(HttpServletRequest request) {
        if (request != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                loginHistoryService.recordLogout(session.getId());
                session.invalidate();
            }
        }
        SecurityContextHolder.clearContext();
    }
}
