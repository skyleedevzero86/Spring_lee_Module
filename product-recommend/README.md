<img width="1672" height="941" alt="image" src="https://github.com/user-attachments/assets/22eee81d-b62a-4453-b13b-a11570f04b06" />

# Product Recommend — 추천 엔진

Spring Boot **4.1.1** / Java **25** / Gradle KTS  
JPA + **pgvector HNSW** / **Elasticsearch dense_vector kNN** + Spring AI + Vue 3

## 파이프라인

```text
Query Understanding → Filtered Vector/Text Search → RRF
  → Personalized Score → MMR → Reranker → Evidence → LLM/API
```

## 벡터 모드 (`app.vector.mode`)

| mode            | 설명                                         |
| --------------- | -------------------------------------------- |
| `memory`        | 로컬 인메모리 cosine (기본 local 프로필)     |
| `pgvector`      | Postgres + HNSW cosine                       |
| `elasticsearch` | Elasticsearch dense_vector kNN (+ 메타 필터) |

ES 학습용 실행:

```powershell
cd product-recommend
docker compose up -d postgres elasticsearch
.\gradlew.bat bootRun --args="--spring.profiles.active=elasticsearch"
```

OpenAI 임베딩 + ES:

```powershell
$env:VECTOR_MODE="elasticsearch"
.\gradlew.bat bootRun --args="--spring.profiles.active=openai,elasticsearch"
```

## 핵심 기능

| 기능             | 설명                                                             |
| ---------------- | ---------------------------------------------------------------- |
| 조건부 벡터 검색 | category/brand/price/color/`excludeBrand` 필터 + cosine          |
| 개인화 랭킹      | vector·preference·keyword·popularity·freshness·business 가중합   |
| Feedback Loop    | impression/click/wish/cart/purchase + CTR                        |
| Reranker         | heuristic rerank                                                 |
| MMR              | 브랜드/카테고리 다양성                                           |
| Evidence         | 추천 근거를 먼저 생성 후 LLM 설명                                |
| Outbox           | PRODUCT_EMBEDDING_REQUESTED 트랜잭션 아웃박스 (+ optional Kafka) |

## 실행

```powershell
cd product-recommend
docker compose up -d
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

실벡터/챗 (pgvector):

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=openai"
```

## API

| Method | Path                                     | 설명                    |
| ------ | ---------------------------------------- | ----------------------- |
| POST   | `/api/v1/products`                       | 상품 생성               |
| POST   | `/api/v1/products/recommend`             | 개인화 추천 파이프라인  |
| POST   | `/api/v1/products/events`                | 행동/피드백 이벤트      |
| GET    | `/api/v1/products/stats/recommendations` | CTR                     |
| POST   | `/api/v1/products/search`                | KNN/하이브리드          |
| POST   | `/api/v1/products/chat`                  | Evidence 기반 구조화 챗 |
| POST   | `/api/v1/products/embeddings/retry`      | 임베딩 재시도           |
| POST   | `/api/v1/products/embeddings/reindex`    | 모델 재인덱싱           |

## 테스트

```powershell
.\gradlew.bat test
```

Testcontainers: Postgres(pgvector) + Elasticsearch 8.15 (`TestcontainersConfiguration`)
