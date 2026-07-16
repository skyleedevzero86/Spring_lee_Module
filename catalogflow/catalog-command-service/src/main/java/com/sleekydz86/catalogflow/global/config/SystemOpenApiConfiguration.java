package com.sleekydz86.catalogflow.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemOpenApiConfiguration {

	@Bean
	OpenAPI catalogSystemOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("CatalogFlow System API")
						.description("메인 Command 서비스(8081) 시스템 API 문서")
						.version("v1"))
				.addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
				.components(new Components()
						.addSecuritySchemes("bearer-jwt", new SecurityScheme()
								.name("bearer-jwt")
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
