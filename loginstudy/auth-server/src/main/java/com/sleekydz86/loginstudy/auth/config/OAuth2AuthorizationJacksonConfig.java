package com.sleekydz86.loginstudy.auth.config;

import com.sleekydz86.loginstudy.auth.security.AuthUser;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
public class OAuth2AuthorizationJacksonConfig {

	@Bean
	JsonMapper authorizationServerJsonMapper() {
		BasicPolymorphicTypeValidator.Builder builder = BasicPolymorphicTypeValidator.builder()
				.allowIfSubType(AuthUser.class)
				.allowIfSubType("com.sleekydz86.loginstudy.auth.");
		OAuth2AuthorizationServerJacksonModule authorizationServerJacksonModule =
				new OAuth2AuthorizationServerJacksonModule();
		authorizationServerJacksonModule.configurePolymorphicTypeValidator(builder);
		List<JacksonModule> securityModules = SecurityJacksonModules.getModules(
				getClass().getClassLoader(),
				builder);
		return JsonMapper.builder()
				.addModule(authorizationServerJacksonModule)
				.addModules(securityModules)
				.build();
	}

	@Bean
	OAuth2AuthorizationService authorizationService(
			JdbcTemplate jdbcTemplate,
			RegisteredClientRepository registeredClientRepository,
			JsonMapper authorizationServerJsonMapper) {
		JdbcOAuth2AuthorizationService service =
				new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
		service.setAuthorizationRowMapper(
				new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper(
						registeredClientRepository,
						authorizationServerJsonMapper));
		service.setAuthorizationParametersMapper(
				new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationParametersMapper(
						authorizationServerJsonMapper));
		return service;
	}
}
