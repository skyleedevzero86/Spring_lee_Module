package com.sleekydz86.passykey.global.security;

import com.sleekydz86.passykey.domain.service.LoginHistoryService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SessionValidationFilter extends OncePerRequestFilter {

    private final LoginHistoryService loginHistoryService;

    public SessionValidationFilter(LoginHistoryService loginHistoryService) {
        this.loginHistoryService = loginHistoryService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/api/public/") || 
            requestPath.startsWith("/api/webauthn/register/options") ||
            requestPath.startsWith("/api/webauthn/authenticate/options") ||
            requestPath.startsWith("/api/auth/webauthn/authenticate") ||
            requestPath.startsWith("/api/auth/login") ||
            requestPath.equals("/favicon.ico") ||
            requestPath.startsWith("/.well-known/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            SecurityContext securityContext = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            
            if (securityContext != null && securityContext.getAuthentication() != null && 
                securityContext.getAuthentication().isAuthenticated()) {
                
                String sessionId = session.getId();
                try {
                    boolean isSessionActive = loginHistoryService.isSessionActive(sessionId);
                    
                    if (!isSessionActive) {
                        session.invalidate();
                        SecurityContextHolder.clearContext();
                        
                        if (requestPath.startsWith("/api/")) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"세션이 만료되었거나 다른 곳에서 로그인되었습니다\"}");
                        } else {
                            response.sendRedirect("/login?session=expired");
                        }
                        return;
                    }
                } catch (Exception e) {
                    org.slf4j.LoggerFactory.getLogger(SessionValidationFilter.class)
                        .warn("세션 검증 중 오류 발생: {}", e.getMessage());
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
