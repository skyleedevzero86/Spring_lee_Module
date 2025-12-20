package com.sleekydz86.passykey.global.config;

import com.sleekydz86.passykey.global.security.CustomAuthenticationSuccessHandler;
import com.sleekydz86.passykey.global.security.SessionValidationFilter;
import com.sleekydz86.passykey.global.security.UserDetailsServiceAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceAdapter userDetailsService;
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    private final SessionValidationFilter sessionValidationFilter;

    public SecurityConfig(
            @Lazy UserDetailsServiceAdapter userDetailsService,
            CustomAuthenticationSuccessHandler authenticationSuccessHandler,
            SessionValidationFilter sessionValidationFilter) {
        this.userDetailsService = userDetailsService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.sessionValidationFilter = sessionValidationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(sessionValidationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/public/**", "/api/auth/webauthn/authenticate", "/api/auth/login",
                                "/api/auth/logout", "/api/webauthn/**"))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**", "/", "/register", "/login", "/static/**", "/css/**",
                                "/js/**")
                        .permitAll()
                        .requestMatchers("/api/webauthn/register/options", "/api/webauthn/authenticate/options")
                        .permitAll()
                        .requestMatchers("/api/auth/webauthn/authenticate").permitAll()
                        .requestMatchers("/favicon.ico", "/.well-known/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"success\":false,\"message\":\"인증이 필요합니다\"}");
                            } else {
                                response.sendRedirect("/login");
                            }
                        }))
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(authenticationSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .disable())
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        String allowedOriginsEnv = System.getenv("CORS_ALLOWED_ORIGINS");
        List<String> allowedOriginsList;

        if (allowedOriginsEnv != null && !allowedOriginsEnv.isEmpty()) {
            allowedOriginsList = new java.util.ArrayList<>(Arrays.asList(allowedOriginsEnv.split(",")));
        } else {
            allowedOriginsList = new java.util.ArrayList<>(
                    List.of("http://localhost", "http://localhost:80", "http://localhost:8080"));
        }

        configuration.setAllowedOriginPatterns(Arrays.asList("https://*.ngrok.io", "https://*.ngrok-free.app"));
        configuration.setAllowedOrigins(allowedOriginsList);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Arrays.asList("Content-Type", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
