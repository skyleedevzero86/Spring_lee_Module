package com.sleekydz86.catalogflow.global.config;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({AppProperties.class, BatchProperties.class})
public class GlobalConfiguration {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
