# CatalogFlow AI

관리자가 상품·이미지를 등록하면 PostgreSQL에 저장하고 RabbitMQ로 이벤트를 발행합니다.  
조회 서비스는 MongoDB Read Model과 Redis 캐시를 구성하며, AI Worker가 상품 설명을 보충합니다.

## 기술 스택

| 구분 | 기술 |
|------|------|
| Runtime | Java 21, Spring Boot 4.1, Gradle Kotlin DSL |
| Architecture | Hexagonal, DDD, CQRS |
| Write | PostgreSQL, JPA, Flyway, Transactional Outbox |
| Messaging | RabbitMQ |
| Read | MongoDB, Redis Cache Aside |
| Storage | LocalStack S3 (Presigned URL) |
| AI | Spring AI 2.0 — Stub / Ollama / Gemini |
| Test | JUnit 5, Testcontainers |

## 모듈

```
catalogflow
├── catalog-domain
├── catalog-command-service   :8081
├── catalog-query-service     :8082
├── ai-enrichment-worker      :8083
├── catalog-batch-service     :8084
├── event-contract
├── test-support
└── docker
```

## 인프라 실행

```powershell
cd docker
docker compose up -d
```

| 서비스 | 포트 |
|--------|------|
| PostgreSQL | 5432 |
| RabbitMQ | 5672 / 15672 |
| MongoDB | 27017 |
| Redis | 6379 |
| LocalStack S3 | 4566 |
| Ollama (profile `ai`) | 11434 |

Ollama 포함 실행:

```powershell
docker compose --profile ai up -d
```

## 애플리케이션 실행

```powershell
.\gradlew.bat :catalog-command-service:bootRun
.\gradlew.bat :catalog-query-service:bootRun
.\gradlew.bat :ai-enrichment-worker:bootRun
```

## AI Provider 설정

`ai-enrichment-worker`의 `app.ai.provider`로 전환합니다.

| 값 | 설명 |
|----|------|
| `stub` | 규칙 기반 Stub (기본, 테스트/로컬 기본값) |
| `ollama` | 로컬 Ollama (`app.ai.ollama.*`) |
| `gemini` | Google Gemini (`app.ai.gemini.api-key` 또는 `GOOGLE_API_KEY`) |

실패 시 Stub 폴백:

```properties
app.ai.provider=ollama
app.ai.fallback-enabled=true
app.ai.model-name=ollama-llama3.2
```

Gemini 예시:

```properties
app.ai.provider=gemini
app.ai.model-name=gemini-2.0-flash
app.ai.gemini.api-key=YOUR_KEY
app.ai.fallback-enabled=true
```

## 주요 API (Command)

| Method | Path | 설명 |
|--------|------|------|
| `POST` | `/api/v1/products` | 상품 등록 |
| `PUT` | `/api/v1/products/{id}` | 상품 수정 |
| `PATCH` | `/api/v1/products/{id}/price` | 가격 변경 |
| `POST` | `/api/v1/products/{id}/images/presigned-url` | S3 Presigned URL |
| `POST` | `/api/v1/products/{id}/images` | 이미지 메타데이터 등록 |
| `POST` | `/api/v1/products/{id}/ai-enrichment` | AI 가공 요청 |
| `POST` | `/api/v1/products/{id}/ai-enrichment/approve` | AI 결과 승인 |
| `POST` | `/api/v1/products/{id}/publish` | 게시 |
| `POST` | `/api/v1/products/{id}/suspend` | 판매 중지 |

## 이벤트 흐름

```
Command API
  → Domain Event → Outbox → RabbitMQ
      ├─ Query Service → MongoDB Projection / Redis Evict
      └─ AI Worker → Stub|Ollama|Gemini → Completed/Failed
            → Command 결과 반영 → Outbox → Query
```

## 테스트

```powershell
.\gradlew.bat clean test
```