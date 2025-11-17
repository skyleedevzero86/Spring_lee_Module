# 토스 페이먼츠 결제 시스템

토스 페이먼츠를 활용한 풀스택 결제 시스템 프로젝트입니다.

## 프로젝트 개요

이 프로젝트는 Next.js 프론트엔드와 Spring Boot 백엔드로 구성된 결제 시스템입니다. 토스 페이먼츠 API를 연동하여 결제 생성, 승인, 환불, 조회 기능을 제공합니다. Hexagonal Architecture 기반으로 설계되었으며, Circuit Breaker와 Retry 패턴을 적용하여 안정성을 확보했습니다.

## 프로젝트 구조

```
spring_react_payment/
├── pay_front_nextjs/   # Next.js 프론트엔드 (최신)
│   ├── src/
│   │   ├── domain/         # 도메인 레이어
│   │   ├── infrastructure/ # 인프라 레이어
│   │   ├── application/    # 애플리케이션 레이어
│   │   ├── components/      # UI 컴포넌트
│   │   ├── hooks/           # Custom Hooks
│   │   └── app/             # Next.js 페이지
│   └── package.json
│
├── pay_front/          # React + Vite 프론트엔드 (레거시)
│   └── ...
│
├── toas_payment2_v2/   # Spring Boot 백엔드 (최신)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   ├── domain/      # 도메인 레이어 (Hexagonal Architecture)
│   │   │   │   └── global/      # 공통 인프라
│   │   │   └── resources/      # 설정 파일
│   │   └── test/               # 테스트 코드
│   └── build.gradle
│
└── toas_payment2/      # Spring Boot 백엔드 (레거시)
    └── ...
```

## 기술 스택

### 프론트엔드 (pay_front_nextjs)

- **Next.js 16** - React 프레임워크
- **TypeScript** - 타입 안정성
- **React 19** - UI 라이브러리
- **React Query** - 서버 상태 관리 및 캐싱
- **Zustand** - 클라이언트 상태 관리
- **React Hook Form + Zod** - 폼 처리 및 검증
- **Tailwind CSS** - 스타일링
- **Axios** - HTTP 클라이언트
- **Toss Payments SDK** - 결제 연동
- **Playwright** - E2E 테스트

### 백엔드 (toas_payment2_v2)

- **Java 21** - 프로그래밍 언어
- **Spring Boot 3.5.7** - 프레임워크
- **Hexagonal Architecture** - 아키텍처 패턴
- **PostgreSQL** - 관계형 데이터베이스
- **Redis** - 캐싱 및 세션 관리
- **Spring Data JPA** - 데이터베이스 접근
- **Resilience4j** - Circuit Breaker, Retry 패턴
- **Micrometer + Prometheus** - 메트릭 수집
- **Zipkin** - 분산 추적
- **Gradle** - 빌드 도구

## 아키텍처

### 백엔드 아키텍처 (Hexagonal Architecture)

백엔드는 Hexagonal Architecture (Ports & Adapters) 패턴을 따릅니다:

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

주요 특징:

- 도메인 로직과 인프라 분리
- 포트와 어댑터 패턴으로 외부 의존성 최소화
- Rich Domain Model 패턴 적용
- Value Object를 통한 타입 안정성
- 이벤트 기반 아키텍처

### 프론트엔드 아키텍처 (Clean Architecture)

프론트엔드는 Clean Architecture 원칙을 따릅니다:

- **Domain Layer**: 비즈니스 로직과 타입 정의
- **Infrastructure Layer**: 외부 시스템과의 통신 (API, HTTP)
- **Application Layer**: 유스케이스 및 서비스 로직
- **Presentation Layer**: UI 컴포넌트 및 페이지

주요 특징:

- 코드 스플리팅 및 동적 import
- 재시도 로직 (exponential backoff)
- 보안 강화 (토큰 관리, CSP 헤더)
- 캐싱 전략 (React Query)
- 관찰성 확보 (로깅, 에러 추적, 성능 모니터링)

## 시작하기

### 필수 요구사항

- **프론트엔드**

  - Node.js 18 이상
  - pnpm 9 이상

- **백엔드**
  - Java 21 이상
  - PostgreSQL 12 이상
  - Redis 6 이상
  - Gradle 7.5 이상

### 설치 및 실행

#### 1. 데이터베이스 설정

PostgreSQL 데이터베이스 생성:

```sql
CREATE DATABASE payment_db;
```

Redis 서버 실행 (기본 포트 6379)

#### 2. 백엔드 실행

```bash
cd toas_payment2_v2
./gradlew bootRun
```

백엔드 서버는 `http://localhost:8080`에서 실행됩니다.

#### 3. 프론트엔드 실행

새 터미널에서:

```bash
cd pay_front_nextjs
pnpm install
pnpm dev
```

프론트엔드 서버는 `http://localhost:3000`에서 실행됩니다.

### 환경 변수 설정

#### 프론트엔드 (.env.local)

`pay_front_nextjs/.env.local` 파일을 생성하고 다음 내용을 추가하세요:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_ERROR_TRACKING_ENABLED=false
NEXT_PUBLIC_PERFORMANCE_MONITORING_ENABLED=false
```

#### 백엔드 (application.yml)

`toas_payment2_v2/src/main/resources/application.yml` 파일을 수정하거나 환경 변수를 설정하세요:

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

toss:
  api:
    key: your_toss_api_key_here

resilience4j:
  retry:
    configs:
      default:
        maxAttempts: 3
  circuitbreaker:
    configs:
      default:
        failureRateThreshold: 50
```

## 주요 기능

### 회원 관리

- 회원가입 (이메일 중복 검증, 비밀번호 암호화)
- 회원 조회 (ID, 이메일)
- 회원 검색 (이름/이메일, 페이징 지원, Redis 캐싱)
- 비밀번호 재설정

### 결제 기능

- 결제 생성 (주문번호 검증, Payment 엔티티 생성, 토스페이먼츠 API 호출)
- 결제 승인 (결제 상태 검증, 토스페이먼츠 execute API 호출, 결제 정보 업데이트)
- 결제 조회 (사용자 권한 기반 조회, ADMIN: 전체, USER: 본인만, Redis 캐싱)
- 결제 상세 조회 (권한 검증 후 상세 정보 반환, Redis 캐싱)
- 결제 이력 페이징 (페이징 처리, 권한 기반 필터링)
- 결제 상태 조회 (토스페이먼츠 API에서 최신 상태 조회)
- 결제 환불 (환불 가능 여부 검증, 금액 검증, 토스페이먼츠 refund API 호출)
- 결제 콜백 (토스페이먼츠에서 보내는 콜백 처리, 재고 차감)

### 성능 및 안정성

- Circuit Breaker: 외부 API 장애 시 자동 차단
- Retry: 일시적 실패 시 자동 재시도 (최대 3회)
- Fallback: 장애 시 적절한 에러 응답
- Redis 캐싱: 결제 이력/상세 정보 캐싱 (TTL: 1시간)
- 이벤트 기반 아키텍처: 결제 생성/완료/환불 시 이벤트 발행, 비동기 처리

### 모니터링 및 관찰성

- Micrometer를 통한 메트릭 수집
- Prometheus 연동
- Zipkin을 통한 분산 추적

## API 엔드포인트

### 요청 헤더

모든 API 요청에는 다음 헤더가 필요합니다:

- `X-User-Id`: 사용자 ID (필수)
- `X-User-Role`: 사용자 역할 (USER/ADMIN)

### 회원 API

- `POST /api/v1/members` - 회원가입
- `GET /api/v1/members/{id}` - 회원 조회 (ID)
- `GET /api/v1/members/email/{email}` - 회원 조회 (이메일)
- `GET /api/v1/members/search/{type}` - 회원 검색 (이름/이메일, 페이징 지원)
- `POST /api/v1/members/reset-password` - 비밀번호 재설정

### 결제 API

- `POST /api/v1/payments` - 결제 생성
- `POST /api/v1/payments/approve` - 결제 승인
- `GET /api/v1/payments` - 결제 이력 조회 (권한 기반)
- `GET /api/v1/payments/page` - 결제 이력 페이징 조회
- `GET /api/v1/payments/{paymentId}` - 결제 상세 조회
- `POST /api/v1/payments/status` - 결제 상태 조회
- `POST /api/v1/payments/{paymentId}/refund` - 결제 환불
- `POST /api/v1/payments/callback` - 결제 콜백 (토스페이먼츠)

### 에러 응답 형식

```json
{
  "timestamp": "2024-01-01T00:00:00",
  "code": "P001",
  "message": "에러 메시지",
  "detail": "상세 정보"
}
```

## 테스트

### 백엔드 테스트

```bash
cd toas_payment2_v2
./gradlew test
./gradlew integrationTest
./gradlew test jacocoTestReport  # 테스트 커버리지
```

### 프론트엔드 테스트

```bash
cd pay_front_nextjs
pnpm test              # 단위 테스트
pnpm test:watch        # 단위 테스트 감시 모드
pnpm test:coverage     # 커버리지 리포트
pnpm test:e2e          # E2E 테스트
pnpm test:e2e:ui       # E2E 테스트 UI 모드
```

## 빌드

### 백엔드 빌드

```bash
cd toas_payment2_v2
./gradlew build
```

빌드된 JAR 파일은 `build/libs/` 디렉토리에 생성됩니다.

### 프론트엔드 빌드

```bash
cd pay_front_nextjs
pnpm build
```

빌드된 파일은 `.next` 디렉토리에 생성됩니다.

## 프로젝트별 상세 문서

- [프론트엔드 README (Next.js)](./pay_front_nextjs/README.md)
- [백엔드 README (v2)](./toas_payment2_v2/README.md)
- [프론트엔드 README (레거시)](./pay_front/README.md)
- [백엔드 README (레거시)](./toas_payment2/README.md)

## 주요 특징

### Resilience 패턴

- **Circuit Breaker**: 외부 API 장애 시 자동 차단
- **Retry**: 일시적 실패 시 자동 재시도 (최대 3회)
- **Fallback**: 장애 시 적절한 에러 응답

### 캐싱 전략

- Redis를 활용한 결제 이력/상세 정보 캐싱
- 회원 검색 결과 캐싱
- TTL: 1시간

### 성능 최적화

- Redis를 통한 조회 성능 향상
- 이벤트 기반 비동기 처리
- 트랜잭션 분리: 외부 API 호출은 트랜잭션 외부에서 처리
- 인덱싱: 주문번호, 사용자 ID, 결제 토큰에 인덱스 적용
- 평균 응답속도 200ms 이하 목표

### 권한 관리

- 사용자는 본인의 결제 기록만 조회 가능합니다
- 관리자는 모든 사용자의 결제 기록을 조회 및 검색할 수 있습니다
- 관리자 API는 `ADMIN` 역할이 필요합니다

## 개발 가이드

### 코드 스타일

- **백엔드**: Clean Code 및 Hexagonal Architecture 원칙 준수
- **프론트엔드**: TypeScript strict mode 사용, Clean Architecture 원칙 준수
- **테스트**: given/when/then 패턴 사용

### 주석 정책

- 일반 주석은 최소화
- 테스트 코드는 `//given`, `//when`, `//then` 형식만 사용
- 모든 로그 및 예외 메시지는 한국어로 작성
- 페이지에 표시되는 모든 텍스트는 한국어로 작성

### 보안 고려사항

- 비밀번호: BCrypt 암호화
- 입력값 검증: Bean Validation (백엔드), Zod (프론트엔드)
- Value Object를 통한 도메인 값 검증
- 권한 기반 접근 제어 (ADMIN/USER)
- XSS 방지: React의 기본 이스케이핑 활용
- CSP 헤더: Content Security Policy 설정
- 토큰 관리: 암호화된 토큰 저장 및 자동 갱신

## 라이선스

이 프로젝트는 개인 프로젝트입니다.
