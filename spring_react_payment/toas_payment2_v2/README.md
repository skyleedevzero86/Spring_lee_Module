# TOAS Payment System v2

## TL;DR

토스페이먼츠 API를 연동한 결제 시스템 (Hexagonal Architecture 기반, Circuit Breaker/Retry 패턴 적용, 평균 응답속도 200ms 이하)

---

## 데모 / 스크린샷

| 이미지                          | 설명                          |
| ------------------------------- | ----------------------------- |
| `docs/images/architecture.png`  | Hexagonal Architecture 구조도 |
| `docs/images/payment_flow.png`  | 결제 처리 플로우 차트         |
| `docs/images/api_endpoints.png` | API 엔드포인트 목록           |

> 체크리스트: [ ] 이미지 2장 이상 포함, [ ] 상대경로 확인, [ ] 설명 캡션 작성

---

## 프로젝트 구성

Spring Boot 기반 백엔드 결제 시스템.

토스페이먼츠 API와 통신하여 결제 생성, 승인, 환불, 조회 기능을 제공합니다.

- 기술 구조: Spring Boot 3.5.7 + Hexagonal Architecture + PostgreSQL + Redis + Resilience4j

- 내 역할: `전체 아키텍처 설계, 도메인 모델링, API 통신 로직, 예외 처리, 성능 최적화`

---

## 기술 스택

- **Backend:** Spring Boot 3.5.7, Java 21
- **Architecture:** Hexagonal Architecture (Ports & Adapters)
- **Database:** PostgreSQL, Redis (캐싱)
- **External API:** 토스페이먼츠 결제 API
- **Resilience:** Resilience4j (Circuit Breaker, Retry)
- **Monitoring:** Micrometer, Prometheus, Zipkin
- **Build Tool:** Gradle
- **기타:** Lombok, Jackson, Spring Data JPA, Spring Cache

---

## 아키텍처 구조

```
domain/
├── payment/          # 결제 도메인
│   ├── adapter/      # 어댑터 (in/out)
│   ├── application/  # 애플리케이션 서비스
│   ├── model/        # 도메인 모델
│   └── port/         # 포트 인터페이스
└── member/           # 회원 도메인
    ├── adapter/
    ├── application/
    ├── model/
    └── port/

global/               # 공통 인프라
├── config/           # 설정 클래스
├── exception/        # 예외 처리
├── util/             # 유틸리티
└── constants/        # 상수 정의
```

---

## API 통신 구조

- **요청 헤더:**

  - `X-User-Id`: 사용자 ID (필수)
  - `X-User-Role`: 사용자 역할 (USER/ADMIN)

- **API Prefix:** `/api/v1` 고정 프리픽스 사용

- **에러 처리 규약:**

  ```json
  {
    "timestamp": "2024-01-01T00:00:00",
    "code": "P001",
    "message": "에러 메시지",
    "detail": "상세 정보"
  }
  ```

- **외부 API:** 토스페이먼츠 API 연동 (Circuit Breaker, Retry 적용)

---

## 주요 기능

| 기능             | 백엔드 동작                                                       | API 엔드포인트                             |
| ---------------- | ----------------------------------------------------------------- | ------------------------------------------ |
| 결제 생성        | 주문번호 검증, Payment 엔티티 생성, 토스페이먼츠 API 호출         | `POST /api/v1/payments`                    |
| 결제 승인        | 결제 상태 검증, 토스페이먼츠 execute API 호출, 결제 정보 업데이트 | `POST /api/v1/payments/approve`            |
| 결제 조회        | 사용자 권한 기반 조회 (ADMIN: 전체, USER: 본인만), Redis 캐싱     | `GET /api/v1/payments`                     |
| 결제 상세 조회   | 권한 검증 후 상세 정보 반환, Redis 캐싱                           | `GET /api/v1/payments/{paymentId}`         |
| 결제 이력 페이징 | 페이징 처리 (기본 20건), 권한 기반 필터링                         | `GET /api/v1/payments/page`                |
| 결제 상태 조회   | 토스페이먼츠 API에서 최신 상태 조회                               | `POST /api/v1/payments/status`             |
| 결제 환불        | 환불 가능 여부 검증, 금액 검증, 토스페이먼츠 refund API 호출      | `POST /api/v1/payments/{paymentId}/refund` |
| 결제 콜백        | 토스페이먼츠에서 보내는 콜백 처리, 재고 차감                      | `POST /api/v1/payments/callback`           |
| 회원가입         | 이메일 중복 검증, 비밀번호 암호화, 회원 생성                      | `POST /api/v1/members`                     |
| 회원 조회        | 이메일 또는 ID로 회원 조회                                        | `GET /api/v1/members/{id}`                 |
| 회원 검색        | 이름/이메일로 검색, 페이징 지원, Redis 캐싱                       | `GET /api/v1/members/search/{type}`        |
| 비밀번호 재설정  | 이메일 검증, 새 비밀번호 암호화 후 업데이트                       | `POST /api/v1/members/reset-password`      |

---

## 실행 방법

### 사전 요구사항

- Java 21 이상
- PostgreSQL 12 이상
- Redis 6 이상
- Gradle 7.5 이상

### 환경 변수 설정

```bash
# 토스페이먼츠 API 키 설정
export TOSS_API_KEY=your_api_key_here
```

또는 `application.yml`에서 직접 설정:

```yaml
toss:
  api:
    key: your_api_key_here
```

### 데이터베이스 설정

PostgreSQL 데이터베이스 생성:

```sql
CREATE DATABASE payment_db;
```

### Redis 설정

Redis 서버 실행 (기본 포트 6379)

### 애플리케이션 실행

```bash
# Gradle Wrapper 사용
./gradlew bootRun

# 또는 빌드 후 실행
./gradlew build
java -jar build/libs/toas_payment2_v2-0.0.1-SNAPSHOT.jar
```

### 테스트 실행

```bash
./gradlew test
```

---

## 주요 특징

### 1. Hexagonal Architecture

- 도메인 로직과 인프라 분리
- 포트와 어댑터 패턴으로 외부 의존성 최소화
- 테스트 용이성 향상

### 2. Resilience 패턴

- **Circuit Breaker**: 외부 API 장애 시 자동 차단
- **Retry**: 일시적 실패 시 자동 재시도 (최대 3회)
- **Fallback**: 장애 시 적절한 에러 응답

### 3. 캐싱 전략

- Redis를 활용한 결제 이력/상세 정보 캐싱
- 회원 검색 결과 캐싱
- TTL: 1시간

### 4. 이벤트 기반 아키텍처

- 결제 생성/완료/환불 시 이벤트 발행
- 비동기 처리로 성능 최적화

### 5. 도메인 모델 강화

- Rich Domain Model 패턴 적용
- Value Object를 통한 타입 안정성
- 도메인 로직이 엔티티에 집중

### 6. 모니터링 및 관찰 가능성

- Micrometer를 통한 메트릭 수집
- Prometheus 연동
- Zipkin을 통한 분산 추적

---

## 설정 파일

### application.yml 주요 설정

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_db
    username: postgres
    password: postgres
  data:
    redis:
      host: localhost
      port: 6379

resilience4j:
  retry:
    configs:
      default:
        maxAttempts: 3
  circuitbreaker:
    configs:
      default:
        failureRateThreshold: 50

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

---

## 성능 최적화

- **캐싱**: Redis를 통한 조회 성능 향상
- **비동기 처리**: 이벤트 기반 비동기 처리
- **트랜잭션 분리**: 외부 API 호출은 트랜잭션 외부에서 처리
- **인덱싱**: 주문번호, 사용자 ID, 결제 토큰에 인덱스 적용

---

## 테스트

```bash
# 단위 테스트
./gradlew test

# 통합 테스트
./gradlew integrationTest

# 테스트 커버리지
./gradlew test jacocoTestReport
```

---

## API 문서

### 결제 API

| Method | Endpoint                              | 설명                     |
| ------ | ------------------------------------- | ------------------------ |
| POST   | `/api/v1/payments`                    | 결제 생성                |
| POST   | `/api/v1/payments/approve`            | 결제 승인                |
| GET    | `/api/v1/payments`                    | 결제 이력 조회           |
| GET    | `/api/v1/payments/page`               | 결제 이력 페이징 조회    |
| GET    | `/api/v1/payments/{paymentId}`        | 결제 상세 조회           |
| POST   | `/api/v1/payments/status`             | 결제 상태 조회           |
| POST   | `/api/v1/payments/{paymentId}/refund` | 결제 환불                |
| POST   | `/api/v1/payments/callback`           | 결제 콜백 (토스페이먼츠) |

### 회원 API

| Method | Endpoint                         | 설명               |
| ------ | -------------------------------- | ------------------ |
| POST   | `/api/v1/members`                | 회원가입           |
| GET    | `/api/v1/members/{id}`           | 회원 조회 (ID)     |
| GET    | `/api/v1/members/email/{email}`  | 회원 조회 (이메일) |
| GET    | `/api/v1/members/search/{type}`  | 회원 검색          |
| POST   | `/api/v1/members/reset-password` | 비밀번호 재설정    |

---

## 보안

- 비밀번호: BCrypt 암호화
- 입력값 검증: Bean Validation
- Value Object를 통한 도메인 값 검증
- 권한 기반 접근 제어 (ADMIN/USER)

---

## 참고 문서

- [리팩토링 요약](./REFACTORING_SUMMARY.md)
- [토스페이먼츠 API 문서](https://docs.toss.im/)

---

## 배포

### Docker를 사용한 배포

```bash
docker build -t toas-payment2-v2 .
docker run -p 8080:8080 \
  -e TOSS_API_KEY=your_api_key \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/payment_db \
  toas-payment2-v2
```

### 환경별 설정

- 개발: `application-dev.yml`
- 운영: `application-prod.yml`

---

## 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.

---

## 라이선스

이 프로젝트는 내부 프로젝트입니다.
