package com.sleekydz86.payment2v2.global.config;

import com.sleekydz86.payment2v2.global.util.InputSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InputValidationConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InputValidationInterceptor())
                .addPathPatterns("/api/**");
    }

    public static class InputValidationInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(
                HttpServletRequest request,
                HttpServletResponse response,
                Object handler
        ) {
            if ("POST".equalsIgnoreCase(request.getMethod()) ||
                "PUT".equalsIgnoreCase(request.getMethod()) ||
                "PATCH".equalsIgnoreCase(request.getMethod())) {
                return validateRequest(request, response);
            }
            return true;
        }

        private boolean validateRequest(HttpServletRequest request, HttpServletResponse response) {
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                return true;
            }

            request.getParameterMap().forEach((key, values) -> {
                for (String value : values) {
                    if (InputSanitizer.containsSqlInjection(value)) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    }
                    if (InputSanitizer.containsXss(value)) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    }
                }
            });

            return response.getStatus() != HttpServletResponse.SC_BAD_REQUEST;
        }
    }
}

