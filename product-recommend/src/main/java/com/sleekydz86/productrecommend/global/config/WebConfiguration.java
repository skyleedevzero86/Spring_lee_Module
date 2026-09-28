package com.sleekydz86.productrecommend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration {

	@Bean
	WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**")
					.allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
					.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
					.allowedHeaders("*")
					.allowCredentials(true)
					.exposedHeaders("X-Conversation-Id");
			}
		};
	}
}
