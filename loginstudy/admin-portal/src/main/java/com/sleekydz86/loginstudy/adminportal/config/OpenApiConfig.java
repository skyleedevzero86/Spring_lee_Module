package com.sleekydz86.loginstudy.adminportal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI adminPortalOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("LoginStudy Admin Portal")
						.description("관리자 OIDC 포털(MVC). ROLE_ADMIN 인가가 필요합니다.")
						.version("v1"))
				.servers(List.of(new Server().url("http://localhost:8082").description("local")));
	}
}
