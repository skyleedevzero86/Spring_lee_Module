package com.sleekydz86.loginstudy.member.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	public static final String BEARER_JWT = "bearer-jwt";

	@Bean
	OpenAPI memberServiceOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("LoginStudy Member Service API")
						.description("OAuth2 Resource Server 회원 API. JWT의 issuer/audience/scope/role을 검증합니다.")
						.version("v1")
						.contact(new Contact().name("LoginStudy").email("admin@loginstudy.local"))
						.license(new License().name("Private Learning Project")))
				.servers(List.of(new Server().url("http://localhost:8083").description("local")))
				.components(new Components().addSecuritySchemes(BEARER_JWT, new SecurityScheme()
						.name(BEARER_JWT)
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.description("auth-server가 발급한 Access Token")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_JWT));
	}
}
