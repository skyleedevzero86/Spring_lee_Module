package com.sleekydz86.loginstudy.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	public static final String SESSION_COOKIE = "auth-session";

	@Bean
	OpenAPI authServerOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("LoginStudy Auth Server API")
						.description("OAuth2 Authorization Server / OIDC Provider 및 Admin Persistence API")
						.version("v1")
						.contact(new Contact().name("LoginStudy").email("admin@loginstudy.local"))
						.license(new License().name("Private Learning Project")))
				.servers(List.of(new Server().url("http://localhost:9000").description("local")))
				.components(new Components().addSecuritySchemes(SESSION_COOKIE, new SecurityScheme()
						.name("AUTHSESSION")
						.type(SecurityScheme.Type.APIKEY)
						.in(SecurityScheme.In.COOKIE)
						.description("폼 로그인 세션 쿠키 (ROLE_ADMIN)")));
	}
}
