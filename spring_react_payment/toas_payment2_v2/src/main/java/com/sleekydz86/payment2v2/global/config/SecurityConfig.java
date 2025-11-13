package com.sleekydz86.payment2v2.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    @Bean
    public SecurityHeadersFilter securityHeadersFilter() {
        return new SecurityHeadersFilter();
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor())
                .addPathPatterns("/api/**");
    }

    public static class SecurityHeadersFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                jakarta.servlet.FilterChain filterChain
        ) throws jakarta.servlet.ServletException, IOException {
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("X-XSS-Protection", "1; mode=block");
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            response.setHeader("Content-Security-Policy", "default-src 'self'");
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            filterChain.doFilter(request, response);
        }
    }

    public static class RateLimitInterceptor implements HandlerInterceptor {
        private static final int MAX_REQUESTS = 100;
        private static final long WINDOW_MS = 60000;
        private final ConcurrentHashMap<String, RequestWindow> requestCounts = new ConcurrentHashMap<>();

        @Override
        public boolean preHandle(
                HttpServletRequest request,
                HttpServletResponse response,
                Object handler
        ) {
            String clientId = getClientId(request);
            RequestWindow window = requestCounts.computeIfAbsent(
                    clientId,
                    k -> new RequestWindow()
            );

            long now = System.currentTimeMillis();
            if (now - window.startTime > WINDOW_MS) {
                window.reset(now);
            }

            if (window.count.incrementAndGet() > MAX_REQUESTS) {
                response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
                return false;
            }

            return true;
        }

        private String getClientId(HttpServletRequest request) {
            String userId = request.getHeader("X-User-Id");
            if (userId != null) {
                return "user:" + userId;
            }
            return request.getRemoteAddr();
        }

        private static class RequestWindow {
            long startTime = System.currentTimeMillis();
            AtomicInteger count = new AtomicInteger(0);

            void reset(long newStartTime) {
                startTime = newStartTime;
                count.set(0);
            }
        }
    }
}

