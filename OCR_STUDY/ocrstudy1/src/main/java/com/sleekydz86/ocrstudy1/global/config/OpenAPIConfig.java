package com.sleekydz86.ocrstudy1.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI ocrStudyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OCR Study 01 API")
                        .version("v1.0.0")
                        .description("""
                                OCR Study 01은 이미지 기반 문서 인식 및 분석 플랫폼입니다.
                                
                                주요 기능:
                                - 이미지 업로드 및 OCR 텍스트 추출 (Tesseract 기반)
                                - 얼굴 인식 및 비교 (DJL 기반)
                                - 문서 타입 자동 감지 (영수증, 신분증, 계약서 등)
                                - AI 기반 문서 분석 및 진위 검증 (Spring AI 사용)
                                - 민감 정보 암호화 저장 (AES-256-GCM)
                                - MinIO 기반 파일 스토리지
                                - Oracle DB 기반 데이터 저장
                                
                                아키텍처:
                                - DDD (Domain-Driven Design)
                                - 헥사고날 아키텍처 (Hexagonal Architecture)
                                - 클린 아키텍처 원칙 적용
                                
                                지원 파일 형식:
                                - JPEG, PNG, BMP, GIF
                                - 최대 50MB
                                
                                보안:
                                - 민감 정보는 AES-256-GCM으로 암호화하여 저장
                                - API Key를 통한 접근 제어 (프로덕션 환경 권장)
                                """)
                        .contact(new Contact()
                                .name("OCR Study Team")
                                .email("support@ocrstudy.com")
                                .url("https://github.com/sleekydz86/ocr-study"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("https://api.ocrstudy.com")
                                .description("프로덕션 서버")
                ));
    }
}