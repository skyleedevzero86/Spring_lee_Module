# 토스 페이먼츠 결제 시스템

토스 페이먼츠를 활용한 풀스택 결제 시스템 프로젝트입니다.

## 프로젝트 개요

이 프로젝트는 React 프론트엔드와 Spring Boot 백엔드로 구성된 결제 시스템입니다. 토스 페이먼츠 API를 연동하여 결제 초기화, 승인, 환불 기능을 제공합니다.

## 프로젝트 구조

```
spring_react_payment/
├── pay_front/          # React 프론트엔드
│   ├── src/
│   │   ├── components/    # UI 컴포넌트
│   │   ├── hooks/         # Custom Hooks
│   │   ├── lib/           # API 클라이언트 및 유틸리티
│   │   ├── pages/         # 페이지 컴포넌트
│   │   └── types/         # TypeScript 타입 정의
│   └── package.json
│
└── toas_payment2/      # Spring Boot 백엔드
    ├── src/
    │   ├── main/
    │   │   ├── java/      # Java 소스 코드
    │   │   └── resources/  # 설정 파일
    │   └── test/          # 테스트 코드
    └── build.gradle
```

## 기술 스택

### 프론트엔드 (pay_front)

- **React 19** - UI 라이브러리
- **TypeScript** - 타입 안정성
- **Vite** - 빌드 도구
- **React Router DOM** - 라우팅
- **Tailwind CSS** - 스타일링
- **shadcn/ui** - UI 컴포넌트
- **Axios** - HTTP 클라이언트
- **Toss Payments SDK** - 결제 연동

### 백엔드 (toas_payment2)

- **Java 21** - 프로그래밍 언어
- **Spring Boot 3.5.7** - 프레임워크
- **Spring Security** - 인증 및 보안
- **Spring Data JPA** - 데이터베이스 접근
- **H2 Database** - 인메모리 데이터베이스
- **JWT** - 토큰 기반 인증
- **BCrypt** - 비밀번호 암호화
- **Swagger/OpenAPI** - API 문서화
- **Gradle** - 빌드 도구

## 아키텍처

### 백엔드 아키텍처 (DDD)

백엔드는 Domain-Driven Design 원칙을 따릅니다:

- **Domain Layer**: 비즈니스 로직 및 도메인 모델
- **Application Layer**: Use Case 및 DTO
- **Infrastructure Layer**: Repository 구현, 외부 API 클라이언트, 보안 구현
- **Presentation Layer**: REST API 컨트롤러

### 프론트엔드 아키텍처

- **Pages**: 라우트별 페이지 컴포넌트
- **Components**: 재사용 가능한 UI 컴포넌트
- **Hooks**: 비즈니스 로직을 캡슐화한 Custom Hooks
- **Services**: API 호출을 담당하는 서비스 레이어
- **Types**: TypeScript 타입 정의

## 시작하기

### 필수 요구사항

- **프론트엔드**

  - Node.js 18 이상
  - pnpm 9 이상

- **백엔드**
  - Java 21 이상
  - Gradle 8.x 이상

### 설치 및 실행

#### 1. 백엔드 실행

```bash
cd toas_payment2
./gradlew bootRun
```

백엔드 서버는 `http://localhost:9000`에서 실행됩니다.

#### 2. 프론트엔드 실행

새 터미널에서:

```bash
cd pay_front
pnpm install
pnpm dev
```

프론트엔드 서버는 `http://localhost:3000`에서 실행됩니다.

### 환경 변수 설정

#### 프론트엔드 (.env)

`pay_front/.env` 파일을 생성하고 다음 내용을 추가하세요:

```env
VITE_API_BASE_URL=http://localhost:9000
VITE_TOSS_CLIENT_KEY=your_toss_client_key
```

#### 백엔드 (application.yml)

`toas_payment2/src/main/resources/application.yml` 파일을 수정하거나 환경 변수를 설정하세요:

```yaml
server:
  port: 9000

toss:
  secret-api-key: your_toss_secret_key
  secure-key: your_toss_secure_key

jwt:
  secret: your_jwt_secret_key_minimum_64_characters
  expiration: 86400000
```

## 주요 기능

- ✅ 사용자 회원가입 및 로그인 (JWT 인증)
- ✅ 비밀번호 BCrypt 암호화
- ✅ 결제 초기화
- ✅ 결제 승인 (토스 페이먼츠 연동)
- ✅ 환불 처리
- ✅ Swagger API 문서화
- ✅ 전역 예외 처리
- ✅ CORS 설정

## API 엔드포인트

### 인증 API

- `POST /api/v1/users/register` - 회원가입
- `POST /api/v1/users/login` - 로그인

### 결제 API (JWT 인증 필요)

- `POST /api/v1/purchase/init` - 결제 초기화
- `POST /api/v1/purchase/confirm` - 결제 승인
- `POST /api/v1/purchase/refund` - 환불

자세한 API 문서는 Swagger UI에서 확인할 수 있습니다:

```
http://localhost:9000/swagger-ui.html
```

## 테스트

### 백엔드 테스트

```bash
cd toas_payment2
./gradlew test
```

### 프론트엔드 테스트

```bash
cd pay_front
pnpm test
```

## 빌드

### 백엔드 빌드

```bash
cd toas_payment2
./gradlew build
```

### 프론트엔드 빌드

```bash
cd pay_front
pnpm build
```

빌드된 파일은 `pay_front/dist` 디렉토리에 생성됩니다.

## 프로젝트별 상세 문서

- [프론트엔드 README](./pay_front/README.md)
- [백엔드 README](./toas_payment2/README.md)

## 개발 가이드

### 코드 스타일

- **백엔드**: Clean Code 및 DDD 원칙 준수
- **프론트엔드**: TypeScript strict mode 사용
- **테스트**: given/when/then 패턴 사용

### 주석 정책

- 일반 주석은 최소화
- 테스트 코드는 `//given`, `//when`, `//then` 형식만 사용
- 모든 로그 및 예외 메시지는 한국어로 작성

## 라이선스

이 프로젝트는 개인 프로젝트입니다.
