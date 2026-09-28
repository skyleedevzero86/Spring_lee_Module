# Product Recommend — 벡터 검색 상품 추천

Spring Boot **4.1.1** / Java **25** / Gradle KTS  
JPA(PostgreSQL + **pgvector HNSW cosine**) + Spring AI + Vue 3 + pnpm

## 구조

```text
# CUD
domain/product, recommend
application/port, service
adapter/in/web, mcp
adapter/out/persistence, ai

# global
global/config, web, evaluation, init
```

## 실행

```powershell
cd product-recommend
docker compose up -d
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

로컬은 해시 임베딩 + 메모리 KNN. 실벡터 검색:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=openai"
# 또는 ollama
.\gradlew.bat bootRun --args="--spring.profiles.active=ollama"
```

프론트:

```powershell
cd frontend
pnpm install
pnpm dev
```

## 포트폴리오 포인트

| #   | 항목           | 구현                                                                                 |
| --- | -------------- | ------------------------------------------------------------------------------------ |
| ①   | HNSW + cosine  | `PgVectorIndexInitializer`, `ScoringFunction.cosine()`, `scripts/benchmark-hnsw.sql` |
| ②   | 하이브리드     | pg_trgm + 벡터 → RRF (`hybrid=true`)                                                 |
| ③   | 비동기 임베딩  | AFTER_COMMIT + `@Async`, PENDING/FAILED/`embedding IS NULL` 백필                     |
| ④   | 검색 품질 평가 | Recall@k / MRR 골든셋, RelevancyEvaluator 훅                                         |
| ⑤   | MCP            | `ProductSearchTool` + `spring-ai-starter-mcp-server-webmvc`                          |
| ⑥   | 구조화 출력    | `.entity(RecommendationResponse.class)`                                              |

## MCP

`openai` 프로필로 기동 후 Claude Desktop `claude_desktop_config.json` 예시:

```json
{
  "mcpServers": {
    "product-recommend": {
      "url": "http://127.0.0.1:8087/mcp"
    }
  }
}
```

`ProductSearchTool`의 `searchProducts` / `findSimilarProducts` 등을 외부에서 호출할 수 있습니다.

## API

| Method | Path                  | 설명                    |
| ------ | --------------------- | ----------------------- |
| POST   | `/`                   | 상품 생성               |
| GET    | `/`                   | 전체 조회               |
| GET    | `/{id}`               | 상세                    |
| PUT    | `/{id}`               | 수정                    |
| DELETE | `/{id}`               | 삭제                    |
| POST   | `/search`             | KNN / 하이브리드        |
| GET    | `/{id}/similar`       | 유사 상품               |
| POST   | `/embeddings/reindex` | 모델 불일치 재인덱싱 큐 |
| POST   | `/embeddings/retry`   | PENDING/FAILED/`embedding IS NULL` 재시도 |
| POST   | `/chat`               | 구조화 챗봇             |
| GET    | `/chat/stream`        | SSE                     |

## HNSW 벤치마크

```powershell
# docker compose up 후
psql -h 127.0.0.1 -p 5433 -U recommend -d product_recommend -f scripts/benchmark-hnsw.sql
```

## 테스트

```powershell
.\gradlew.bat test
```
