package com.sleekydz86.passykey.global.security;

import com.sleekydz86.passykey.domain.model.User;
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

    public void setAuthentication(User user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetails(request));
        
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
        
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
    }

    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}

