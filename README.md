# Spring_lee_Module

Spring Boot **4.x** 학습용 모듈 모음입니다.  
기능을 익힐 때마다 아래 **학습 기록**에 내용을 추가합니다.

<br/>

## 프로젝트


| 경로                           | 요약                                                                                                                                                                                                 |
| ------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [loginstudy](./loginstudy)     | 회원·인증 플랫폼 축소판. OAuth2 Authorization Server, OIDC, SSO, Resource Server, Redis Session, PostgreSQL 튜닝을 **Spring Boot 4.1** 기준으로 구성합니다.                                         |
| [catalogflow](./catalogflow)   | 상품 카탈로그 CQRS 플랫폼. Command/Query 분리, RabbitMQ Outbox, MongoDB Read Model, Redis Cache, LocalStack S3, Stub/Ollama/Gemini AI Enrichment를 **Spring Boot 4.1** 기준으로 구성합니다. |


## Spring 학습 기록

학습할 때마다 날짜·주제·실습 모듈을 이어서 적습니다.

### 2026-07-16 — CatalogFlow AI

- **버전**: Spring Boot 4.1.0, Java 21, Gradle Kotlin DSL, Spring AI 2.0.0
- **모듈**: `catalog-domain` · `catalog-command-service` · `catalog-query-service` · `ai-enrichment-worker` · `catalog-batch-service` · `event-contract` · `test-support`
- **익힌 기능**
  - 헥사고날 + DDD Aggregate / Domain Event
  - CQRS (PostgreSQL Write / MongoDB Read)
  - Transactional Outbox + RabbitMQ 토폴로지 · DLQ · 멱등 소비
  - Redis Cache Aside · 이벤트 기반 무효화
  - LocalStack S3 Presigned Upload
  - AI Enrichment Worker (Stub / Ollama / Gemini) + 결과 반영
  - Docker Compose · Testcontainers

### 2026-07-15 — LoginStudy Identity Platform

- **버전**: Spring Boot 4.1.0, Java 21, Gradle Kotlin DSL
- **모듈**: `auth-server` · `member-service` · `user-portal` · `admin-portal`
- **익힌 기능**
  - Spring Security OAuth2 Authorization Server / OIDC / SSO
  - Resource Server JWT
  - Spring Session + Redis Cluster
  - Spring Data JPA + Flyway, PostgreSQL 검색 인덱스 · Keyset 페이징
  - MSA 포트 분리 및 포털 → member-service 연동
  - springdoc OpenAPI / Swagger UI
  - Actuator · 보안 시나리오 테스트
