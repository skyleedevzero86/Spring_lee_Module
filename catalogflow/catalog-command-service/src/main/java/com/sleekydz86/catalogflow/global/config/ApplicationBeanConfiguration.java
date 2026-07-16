package com.sleekydz86.catalogflow.global.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationBeanConfiguration {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
