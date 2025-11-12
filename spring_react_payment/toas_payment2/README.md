# 토스 페이먼츠 결제 시스템

토스 페이먼츠를 활용한 결제 시스템 백엔드 API 서버입니다.

## 기술 스택

- **Java 21** - 프로그래밍 언어
- **Spring Boot 3.5.7** - 프레임워크
- **Spring Security** - 인증 및 보안
- **Spring Data JPA** - 데이터베이스 접근
- **H2 Database** - 인메모리 데이터베이스
- **JWT** - 토큰 기반 인증
- **BCrypt** - 비밀번호 암호화
- **Swagger/OpenAPI** - API 문서화
- **Gradle** - 빌드 도구
- **Lombok** - 보일러플레이트 코드 제거

## 아키텍처

이 프로젝트는 **Domain-Driven Design (DDD)** 원칙을 따릅니다.

### 계층 구조

```
domain/              # 도메인 계층
  ├── order/         # 주문 도메인
  ├── user/          # 사용자 도메인
  └── payment/       # 결제 도메인 인터페이스

application/         # 애플리케이션 계층
  ├── dto/           # 데이터 전송 객체 (Record)
  └── usecase/       # Use Case

infrastructure/      # 인프라 계층
  ├── persistence/   # Repository 구현
  ├── external/      # 외부 API 클라이언트
  └── security/      # 보안 구현

presentation/        # 프레젠테이션 계층
  └── Controller     # REST API 엔드포인트
```

### 주요 설계 원칙

- **SOLID 원칙** 준수
- **Java Record**를 활용한 불변 객체
- **Value Object** 패턴 적용
- **Use Case** 패턴으로 비즈니스 로직 분리
- **의존성 역전 원칙 (DIP)** 준수

## 주요 기능

- 사용자 회원가입 및 로그인 (JWT 인증)
- 비밀번호 BCrypt 암호화
- 결제 초기화
- 결제 승인
- 환불 처리
- Swagger API 문서화

## 필수 요구사항

- Java 21 이상
- Gradle 8.x 이상

## 환경 설정

### application.yml

`src/main/resources/application.yml` 파일에서 설정을 관리합니다.

주요 설정:

- 서버 포트: 9000
- 데이터베이스: H2 (인메모리)
- JWT 시크릿 키 및 만료 시간
- 토스 페이먼츠 API 키

### 환경 변수

환경 변수를 사용하여 설정을 오버라이드할 수 있습니다:

```bash
export SERVER_PORT=9000
export JWT_SECRET=your_secret_key
export TOSS_SECRET_API_KEY=your_toss_secret_key
```

서버는 기본적으로 `http://localhost:9000`에서 실행됩니다.

## API 엔드포인트

### 인증 API

#### 회원가입

```
POST /api/v1/users/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "name": "사용자 이름"
}
```

#### 로그인

```
POST /api/v1/users/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "message": "로그인 성공",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "name": "사용자 이름",
    "token": "jwt_token_here"
  }
}
```

### 결제 API

모든 결제 API는 JWT 토큰 인증이 필요합니다.

요청 헤더에 토큰을 포함하세요:

```
Authorization: Bearer {jwt_token}
```

#### 결제 초기화

```
POST /api/v1/purchase/init
Authorization: Bearer {token}
Content-Type: application/json

{
  "eventId": 1,
  "amount": 50000
}
```

#### 결제 승인

```
POST /api/v1/purchase/confirm
Authorization: Bearer {token}
Content-Type: application/json

{
  "paymentKey": "payment_key_from_toss",
  "orderId": "order_id",
  "orderName": "예매 티켓",
  "amount": 50000
}
```

#### 환불

```
POST /api/v1/purchase/refund
Authorization: Bearer {token}
Content-Type: application/json

{
  "orderId": "order_id",
  "paymentKey": "payment_key",
  "refundReason": "환불 사유",
  "paidAmount": 50000
}
```

## Swagger API 문서

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

```
http://localhost:9000/swagger-ui.html
```

## 프로젝트 구조

```
toas_payment2/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/sleekydz86/toaspayment/
│   │   │       ├── application/        # 애플리케이션 계층
│   │   │       │   ├── dto/           # DTO (Record)
│   │   │       │   └── usecase/       # Use Case
│   │   │       ├── domain/            # 도메인 계층
│   │   │       │   ├── order/         # 주문 도메인
│   │   │       │   ├── user/         # 사용자 도메인
│   │   │       │   └── payment/       # 결제 도메인 인터페이스
│   │   │       ├── infrastructure/    # 인프라 계층
│   │   │       │   ├── persistence/  # Repository 구현
│   │   │       │   ├── external/     # 외부 API 클라이언트
│   │   │       │   └── security/     # 보안 구현
│   │   │       ├── presentation/      # 프레젠테이션 계층
│   │   │       │   └── Controller     # REST API
│   │   │       ├── config/           # 설정 클래스
│   │   │       └── exception/         # 예외 처리
│   │   └── resources/
│   │       └── application.yml       # 설정 파일
│   └── test/
│       └── java/                     # 테스트 코드
├── build.gradle                      # Gradle 빌드 설정
└── settings.gradle                  # Gradle 프로젝트 설정
```

## 도메인 모델

### Order (주문)

- 주문 생성 및 상태 관리
- 결제 완료 처리
- 환불 처리

### User (사용자)

- 회원가입 및 로그인
- 비밀번호 암호화

### Value Objects

- `OrderId`: 주문 ID 캡슐화
- `Money`: 금액 검증 및 연산

## 실행 방법

### Gradle Wrapper 사용

```bash
./gradlew bootRun
```

### 빌드

```bash
./gradlew build
```

### 테스트 실행

```bash
./gradlew test
```

## 데이터베이스

기본적으로 H2 인메모리 데이터베이스를 사용합니다.

H2 콘솔 접속:

```
http://localhost:9000/h2-console
```

JDBC URL: `jdbc:h2:mem:testdb`
사용자명: `sa`
비밀번호: (비어있음)

## 보안

- JWT 토큰 기반 인증
- BCrypt 비밀번호 암호화
- Spring Security를 통한 엔드포인트 보호
- CORS 설정 (프론트엔드: http://localhost:3000)

## 예외 처리

모든 예외는 `GlobalExceptionHandler`에서 처리되며, 한국어 메시지를 반환합니다.

## 로깅

모든 로그 메시지는 한국어로 작성되었습니다.

## 라이선스

이 프로젝트는 개인 프로젝트입니다.
