package com.sleekydz86.monitoring.logstack_s3.global.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sleekydz86.monitoring.logstack_s3.domain.message.DomainMessages;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminApiKeyFilter extends OncePerRequestFilter {

    public static final String ADMIN_API_KEY_HEADER = "X-Admin-Api-Key";

    private final String configuredApiKey;

    public AdminApiKeyFilter(@Value("${logstack.admin.api-key}") String configuredApiKey) {
        this.configuredApiKey = configuredApiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/admin");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String provided = request.getHeader(ADMIN_API_KEY_HEADER);
        if (provided == null || provided.isBlank()) {
            writeUnauthorized(response, DomainMessages.ADMIN_API_KEY_REQUIRED);
            return;
        }
        if (!configuredApiKey.equals(provided)) {
            writeUnauthorized(response, DomainMessages.ADMIN_API_KEY_INVALID);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"message\":\"" + escaped + "\"}");
    }
}
