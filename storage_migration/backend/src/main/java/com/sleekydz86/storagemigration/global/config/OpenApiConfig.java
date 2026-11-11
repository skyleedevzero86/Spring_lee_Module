package com.sleekydz86.storagemigration.global.config;

import com.sleekydz86.storagemigration.global.error.ErrorCode;
import com.sleekydz86.storagemigration.global.openapi.ApiErrorCodes;
import com.sleekydz86.storagemigration.global.web.Actor;
import com.sleekydz86.storagemigration.global.web.ActorArgumentResolver;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
        return new OpenAPI()
                .info(new Info()
                        .title("스토리지 이관 API")
                        .version("0.0.1")
                        .description("""
                                MinIO·S3 파일을 업로드·조회하고, 파일 정보와 파일 자체(BLOB)를 프로시저로 DB에 저장합니다.

                                **사용 순서**
                                1. DB 연결: 접속 정보 등록 → 연결 테스트 → 이 DB 사용 (테이블·프로시저 자동 준비)
                                2. 스토리지 서버: MinIO 콘솔(9001) · MinIO S3 API(9000) · AWS S3 등록 → 연결 테스트
                                3. 파일: 업로드하거나 스토리지 파일을 가져와 등록 → DB에 저장(BLOB)

                                **공통 응답 형식**
                                - 성공: `{"success": true, "data": ..., "error": null}`
                                - 실패: `{"success": false, "data": null, "error": {"code": "D002", "message": "...", "detail": "..."}}`

                                **작업 로그**
                                등록·수정·삭제·BLOB 저장은 `SP_SM_*_CUD` 프로시저로 처리하고, SM_AUDIT_LOG에 작업자(X-Worker 헤더)·IP·일시·내용이 남습니다.

                                **DB 연결이 필요한 API**
                                스토리지 서버·파일·작업 로그 API는 사용 중인 DB가 없으면 409 (D002)를 돌려줍니다.
                                """))
                .tags(List.of(
                        new Tag().name("DB 연결").description("작업 데이터를 저장할 DB(Oracle·MySQL·PostgreSQL) 접속 정보 관리. 접속 정보는 서버의 파일에 암호화해 저장합니다."),
                        new Tag().name("스토리지 서버").description("파일을 올릴 MinIO·S3 서버 관리. 정보는 사용 중인 DB의 SM_STORAGE_SERVER에 저장합니다."),
                        new Tag().name("파일").description("파일 업로드·조회·수정·삭제, 파일 내용을 SM_STORED_FILE.CONTENT(BLOB)에 저장·조회"),
                        new Tag().name("작업 로그").description("SM_AUDIT_LOG 조회: 누가 · 어디서 · 언제 · 무엇을")));
    }

    @Bean
    public OperationCustomizer workerHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            boolean usesActor = Arrays.stream(handlerMethod.getMethodParameters())
                    .anyMatch(parameter -> Actor.class.equals(parameter.getParameterType()));
            if (usesActor) {
                operation.addParametersItem(new HeaderParameter()
                        .name(ActorArgumentResolver.WORKER_HEADER)
                        .description("작업자 이름 (URL 인코딩). 작업 로그의 작업자 칸에 남습니다. 비우면 anonymous로 기록됩니다.")
                        .required(false)
                        .example("kim")
                        .schema(new StringSchema()));
            }
            return operation;
        };
    }

    @Bean
    public OperationCustomizer errorResponseCustomizer() {
        return (operation, handlerMethod) -> {
            ApiErrorCodes annotation = handlerMethod.getMethodAnnotation(ApiErrorCodes.class);
            Set<ErrorCode> codes = new LinkedHashSet<>();
            if (annotation != null) {
                codes.addAll(Arrays.asList(annotation.value()));
            }
            codes.add(ErrorCode.INTERNAL_ERROR);
            Map<Integer, List<ErrorCode>> byStatus = codes.stream()
                    .collect(Collectors.groupingBy(code -> code.status().value(), LinkedHashMap::new, Collectors.toList()));
            ApiResponses responses = operation.getResponses() == null ? new ApiResponses() : operation.getResponses();
            byStatus.forEach((status, list) -> {
                String description = list.stream()
                        .map(code -> "`" + code.code() + "` " + code.message())
                        .collect(Collectors.joining("<br>"));
                ErrorCode first = list.get(0);
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("code", first.code());
                error.put("message", first.message());
                error.put("detail", null);
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("success", false);
                body.put("data", null);
                body.put("error", error);
                responses.addApiResponse(String.valueOf(status), new io.swagger.v3.oas.models.responses.ApiResponse()
                        .description(description)
                        .content(new Content().addMediaType("application/json", new MediaType().example(body))));
            });
            operation.setResponses(responses);
            return operation;
        };
    }
}
