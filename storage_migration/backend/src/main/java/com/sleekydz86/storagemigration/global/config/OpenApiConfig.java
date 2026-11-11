package com.sleekydz86.storagemigration.global.config;

import com.sleekydz86.storagemigration.global.web.Actor;
import com.sleekydz86.storagemigration.global.web.ActorArgumentResolver;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import java.util.Arrays;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    static {
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(Actor.class);
    }

    @Bean
    public OpenAPI storageMigrationOpenApi() {
        return new OpenAPI().info(new Info()
                .title("스토리지 이관 API")
                .version("0.0.1")
                .description("MinIO·S3 파일을 업로드·조회하고, 파일 정보와 파일 자체(BLOB)를 프로시저로 DB에 저장합니다. "
                        + "등록·수정·삭제는 모두 SM_AUDIT_LOG에 작업자·IP·일시가 남습니다."));
    }

    @Bean
    public OperationCustomizer workerHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            boolean usesActor = Arrays.stream(handlerMethod.getMethodParameters())
                    .anyMatch(parameter -> Actor.class.equals(parameter.getParameterType()));
            if (usesActor) {
                operation.addParametersItem(new HeaderParameter()
                        .name(ActorArgumentResolver.WORKER_HEADER)
                        .description("작업자 이름 (URL 인코딩). 비우면 anonymous로 기록됩니다.")
                        .required(false)
                        .schema(new StringSchema()));
            }
            return operation;
        };
    }
}
