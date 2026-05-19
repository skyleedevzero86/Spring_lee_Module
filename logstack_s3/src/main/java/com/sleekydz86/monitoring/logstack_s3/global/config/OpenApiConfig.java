package com.sleekydz86.monitoring.logstack_s3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("logstack_s3 파일 API")
                        .description("S3(LocalStack) 파일 업로드·조회·삭제 및 대용량 시드 API")
                        .version("v1"));
    }
}
