package com.sleekydz86.payment2v2.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.core.env.Environment;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {
    
    private final Environment environment;
    
    public SecurityConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().permitAll())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(contentType -> {
                        })
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
                .addFilterBefore(securityHeadersFilter(), org.springframework.security.web.header.HeaderWriterFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-User-Id",
                "X-User-Role",
                "X-Requested-With",
                "X-CSRF-Token",
                "X-XSRF-TOKEN",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-User-Id",
                "X-User-Role"));
        configuration.setMaxAge(3600L);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);
        log.info("CORS 설정 완료: 허용된 Origin={}, Methods={}", 
            configuration.getAllowedOrigins(), 
            configuration.getAllowedMethods());
        
        return source;
    }

    @Bean
    public SecurityHeadersFilter securityHeadersFilter() {
        return new SecurityHeadersFilter();
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev") ||
                       Arrays.asList(environment.getActiveProfiles()).contains("development") ||
                       !Arrays.asList(environment.getActiveProfiles()).contains("prod");
        
        int maxRequests = isDev ? 500 : 100;
        long windowMs = 60000;
        
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);
        log.info("Rate Limit 설정: maxRequests={}, windowMs={}ms, isDev={}", 
            maxRequests, windowMs, isDev);
        
        return new RateLimitInterceptor(maxRequests, windowMs);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor())
                .addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:3001")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization", 
                               "X-User-Id", "X-User-Role", "X-Requested-With",
                               "X-CSRF-Token", "X-XSRF-TOKEN",
                               "Access-Control-Request-Method", "Access-Control-Request-Headers")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.setOrder(org.springframework.core.Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    public void extendMessageConverters(java.util.List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        for (org.springframework.http.converter.HttpMessageConverter<?> converter : converters) {
            if (converter instanceof org.springframework.http.converter.StringHttpMessageConverter) {
                ((org.springframework.http.converter.StringHttpMessageConverter) converter)
                    .setDefaultCharset(java.nio.charset.StandardCharsets.UTF_8);
            }
        }
    }

    public static class SecurityHeadersFilter extends OncePerRequestFilter {
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecurityHeadersFilter.class);
        
        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                jakarta.servlet.FilterChain filterChain) throws jakarta.servlet.ServletException, IOException {

            log.info("요청 수신: {} {} from origin: {}", 
                request.getMethod(), 
                request.getRequestURI(),
                request.getHeader("Origin"));
            
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("X-XSS-Protection", "1; mode=block");
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            filterChain.doFilter(request, response);
        }
    }

    public static class RateLimitInterceptor implements HandlerInterceptor {
        private final int MAX_REQUESTS;
        private final long WINDOW_MS;
        private final ConcurrentHashMap<String, RequestWindow> requestCounts = new ConcurrentHashMap<>();
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RateLimitInterceptor.class);
        
        public RateLimitInterceptor() {
            this(100, 60000);
        }
        
        public RateLimitInterceptor(int maxRequests, long windowMs) {
            this.MAX_REQUESTS = maxRequests;
            this.WINDOW_MS = windowMs;
        }

        @Override
        public boolean preHandle(
                HttpServletRequest request,
                HttpServletResponse response,
                Object handler) {
            String clientId = getClientId(request);
            RequestWindow window = requestCounts.computeIfAbsent(
                    clientId,
                    k -> new RequestWindow());

            long now = System.currentTimeMillis();
            if (now - window.startTime > WINDOW_MS) {
                window.reset(now);
            }

            int currentCount = window.count.incrementAndGet();
            if (currentCount > MAX_REQUESTS) {
                long remainingTime = WINDOW_MS - (now - window.startTime);
                long retryAfterSeconds = (remainingTime / 1000) + 1;
                
                response.setStatus(429);
                response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
                response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS));
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setHeader("X-RateLimit-Reset", String.valueOf(window.startTime + WINDOW_MS));
                response.setContentType("application/json");
                
                log.warn("Rate limit 초과: clientId={}, count={}, limit={}, retryAfter={}초", 
                    clientId, currentCount, MAX_REQUESTS, retryAfterSeconds);
                
                try {
                    response.getWriter().write(
                        String.format(
                            "{\"timestamp\":\"%s\",\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"요청 한도를 초과했습니다. %d초 후 다시 시도해주세요.\",\"detail\":\"1분에 %d개 요청까지 허용됩니다.\"}",
                            java.time.Instant.now().toString(),
                            retryAfterSeconds,
                            MAX_REQUESTS
                        )
                    );
                } catch (IOException e) {
                    log.error("Rate limit 에러 응답 작성 실패", e);
                }
                
                return false;
            }

            response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(MAX_REQUESTS - currentCount));
            response.setHeader("X-RateLimit-Reset", String.valueOf(window.startTime + WINDOW_MS));

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
