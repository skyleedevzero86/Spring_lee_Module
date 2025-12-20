package com.sleekydz86.passykey.global.security;

import com.sleekydz86.passykey.domain.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;

    public CustomAuthenticationSuccessHandler(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        setDefaultTargetUrl("/dashboard");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            try {
                authenticationService.checkAndPreventDuplicateLogin(user, request);
                authenticationService.setAuthentication(user, request, "PASSWORD");
            } catch (IllegalStateException e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
                return;
            }
        }
        
        super.onAuthenticationSuccess(request, response, authentication);
    }
}

