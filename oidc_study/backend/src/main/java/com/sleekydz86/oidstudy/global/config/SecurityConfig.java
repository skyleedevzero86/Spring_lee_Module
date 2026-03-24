package com.sleekydz86.oidstudy.global.config;

import com.sleekydz86.oidstudy.oidc.service.NaverOidcUserService;
import com.sleekydz86.oidstudy.oidc.service.NaverOAuth2UserService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final NaverOidcUserService naverOidcUserService;
    private final NaverOAuth2UserService naverOAuth2UserService;
    private final String frontendBaseUrl;

    public SecurityConfig(
            NaverOidcUserService naverOidcUserService,
            NaverOAuth2UserService naverOAuth2UserService,
            @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl
    ) {
        this.naverOidcUserService = naverOidcUserService;
        this.naverOAuth2UserService = naverOAuth2UserService;
        this.frontendBaseUrl = frontendBaseUrl.replaceAll("/+$", "");
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/session",
                                "/api/logout",
                                "/oauth2/**",
                                "/login/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/dashboard").hasAnyRole("USER", "MANAGER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl(frontendBaseUrl + "/#/my", true)
                        .failureHandler((request, response, exception) -> {
                            String reason = exception.getMessage() == null ? "알 수 없는 오류" : exception.getMessage();
                            log.warn("OAuth2 로그인에 실패했습니다. 원인={}", reason);
                            String encoded = URLEncoder.encode(reason, StandardCharsets.UTF_8);
                            response.sendRedirect(frontendBaseUrl + "/#/my?login=error&reason=" + encoded);
                        })
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(naverOAuth2UserService)
                                .oidcUserService(naverOidcUserService)
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                        })
                )
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                PathPatternRequestMatcher.pathPattern("/api/**")
                        )
                );

        return http.build();
    }
}