package com.sleekydz86.loginstudy.userportal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI userPortalOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("LoginStudy User Portal")
						.description("OIDC 로그인 포털(MVC). 주요 화면 경로를 OpenAPI로 노출합니다.")
						.version("v1"))
				.servers(List.of(new Server().url("http://localhost:8081").description("local")));
	}
}
