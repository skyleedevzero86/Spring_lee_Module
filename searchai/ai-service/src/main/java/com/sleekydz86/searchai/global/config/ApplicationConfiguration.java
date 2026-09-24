package com.sleekydz86.searchai.global.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class ApplicationConfiguration {

	private static final Logger log = LoggerFactory.getLogger(ApplicationConfiguration.class);

	private final AppProperties appProperties;

	public ApplicationConfiguration(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	@PostConstruct
	void configureProxy() {
		AppProperties.Proxy proxy = appProperties.proxy();
		if (proxy == null || !proxy.enabled()) {
			return;
		}
		java.util.List.of("http", "https").forEach(protocol -> {
			System.setProperty(protocol + ".proxyHost", proxy.host());
			System.setProperty(protocol + ".proxyPort", Integer.toString(proxy.port()));
		});
		log.info("시스템 프록시 설정됨: http://{}:{}", proxy.host(), proxy.port());
	}
}
