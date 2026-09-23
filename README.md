# Spring_lee_Module

Spring Boot **4.x** 학습용 모듈 모음입니다.  
기능을 익힐 때마다 아래 **학습 기록**에 내용을 추가합니다.

<br/>

## 프로젝트

| 경로                             | 요약                                                                                                                                                                                        |
| -------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [loginstudy](./loginstudy)       | 회원·인증 플랫폼 축소판. OAuth2 Authorization Server, OIDC, SSO, Resource Server, Redis Session, PostgreSQL 튜닝을 **Spring Boot 4.1** 기준으로 구성합니다.                                 |
| [catalogflow](./catalogflow)     | 상품 카탈로그 CQRS 플랫폼. Command/Query 분리, RabbitMQ Outbox, MongoDB Read Model, Redis Cache, LocalStack S3, Stub/Ollama/Gemini AI Enrichment를 **Spring Boot 4.1** 기준으로 구성합니다. |
| [jvmboard](./jvmboard)           | JVM 런타임 대시보드. MXBean으로 Java/힙/GC/가동시간을 조회하고 Vue 3 Dashboard로 표시합니다. <br>헥사고날·DDD·SOLID를 **Spring Boot 4.1 + Java 27** 기준으로 구성합니다.                    |
| [concurrentlab](./concurrentlab) | Concurrent API Lab. 고객 통합조회(프로필·주문·추천)를 Sequential vs `StructuredTaskScope`로 비교합니다. <br>헥사고날·Strategy·Vue 3· **Java 27 Preview** 기준입니다.                        |
| [failurelab](./failurelab)       | 장애/Timeout Lab. 하위 작업 실패·타임아웃 시 SUCCESS/FAILED/CANCELLED와 예외 전파를 보여줍니다. <br>헥사고날·`StructuredTaskScope`·Vue 3· **Java 27 Preview** 기준입니다.                   |
| [lazylab](./lazylab)             | Lazy Constant Lab. `LazyConstant`로 AI 모델 설정을 최초 접근 시 한 번만 로드합니다. <br>헥사고날·JEP 531·Vue 3·**Java 27 Preview** 기준입니다.                                              |

## Spring 학습 기록

학습할 때마다 날짜·주제·실습 모듈을 이어서 적습니다.

### 2026-09-23 — Lazy Constant Lab

- **버전**: Spring Boot 4.1.1, Java 27, Gradle Kotlin DSL, Vue 3, pnpm
- **모듈**: `lazylab` (backend) · `frontend`
- **익힌 기능**
  - `LazyConstant.of(Supplier)` 지연 초기화
  - 무거운 config.json 로딩·파싱을 최초 수행
  - 첫 접근 Load time ≈ 800ms, 이후 ≈ 0ms 비교 UI

### 2026-09-23 — Failure / Timeout Lab

- **버전**: Spring Boot 4.1.1, Java 27, Gradle Kotlin DSL, Vue 3, pnpm
- **모듈**: `failurelab` (backend) · `frontend`(frontend)
- **익힌 기능**
  - `StructuredTaskScope` + timeout으로 실패 시 sibling 취소
  - Task 상태 SUCCESS / FAILED / CANCELLED 관찰
  - 예외 전파 와 Reason 표시
  - Vue 3 장애 시뮬레이션 UI

### 2026-09-22 — Concurrent API Lab

- **버전**: Spring Boot 4.1.1, Java 27 , Gradle Kotlin DSL, Vue 3, pnpm
- **모듈**: `concurrentlab` (backend) · `frontend` (frontend)
- **익힌 기능**
  - `StructuredTaskScope` 기반 구조화 동시성 vs 순차 실행 지연 비교
  - 헥사고날 + DDD + Strategy · Factory
  - 시뮬레이션 어댑터
  - Vue 3 고객 통합조회 UI

### 2026-09-22 — JVMBoard Dashboard

- **버전**: Spring Boot 4.1.1, Java 27, Gradle Kotlin DSL, Vue 3
- **모듈**: `jvmboard` (backend) · `frontend` (Vue + Vite)
- **익힌 기능**
  - 헥사고날 + DDD
  - `GET /api/system` — Runtime · Memory · GC · Uptime MXBean 스냅샷
  - Vue 3 대시보드 실시간 폴링
  - Gradle Java toolchain (Azul Zulu 27)

### 2026-07-16 — CatalogFlow AI

- **버전**: Spring Boot 4.1.0, Java 21, Gradle Kotlin DSL, Spring AI 2.0.0
- **모듈**: `catalog-domain` · `catalog-command-service` · `catalog-query-service` · `ai-enrichment-worker` · `catalog-batch-service` · `event-contract` · `test-support`
- **익힌 기능**
  - 헥사고날 + DDD Aggregate / Domain Event
  - CQRS
  - Transactional Outbox + RabbitMQ 토폴로지 · DLQ · 멱등 소비
  - Redis Cache Aside · 이벤트 기반 무효화
  - LocalStack S3 Presigned Upload
  - AI Enrichment Worker + 결과 반영
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
