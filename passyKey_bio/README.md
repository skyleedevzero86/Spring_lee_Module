# PassyKey Bio

WebAuthn/Passkey 기반의 비밀번호 없는 인증 시스템입니다. Spring Boot 백엔드와 Next.js 프론트엔드로 구성되어 있으며, 모바일 생체 인증을 완벽하게 지원합니다.

## 목차

- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [시작하기](#시작하기)
- [아키텍처](#아키텍처)
- [API 엔드포인트](#api-엔드포인트)
- [보안](#보안)
- [모바일 생체 인증](#모바일-생체-인증)
- [문제 해결](#문제-해결)

## 주요 기능

- **비밀번호 없는 인증**: WebAuthn/Passkey를 통한 강력한 인증
- **모바일 생체 인증 지원**: 지문/얼굴 인식으로 간편한 로그인
- **사용자 관리**: 사용자 등록, 조회, 중복 확인
- **패스키 관리**: 패스키 등록, 인증, 삭제 (최대 3개)
- **세션 기반 챌린지 관리**: Redis를 활용한 안전한 챌린지 저장
- **리플레이 공격 방지**: 카운터 기반 인증서 검증
- **반응형 디자인**: 모바일과 데스크톱 모두 지원

## 기술 스택

### Backend (`/backend`)

- **Java 21**: 최신 Java 기능 활용
- **Spring Boot 3.5.7**: 웹 애플리케이션 프레임워크
- **Spring Security**: 인증 및 보안
- **MyBatis 3.0.3**: SQL 매퍼 프레임워크
- **MySQL 9.1.0**: 관계형 데이터베이스
- **Redis**: 세션 및 챌린지 캐시
- **WebAuthn4j 0.30.0.RELEASE**: WebAuthn 프로토콜 구현
- **WebAuthn4j Spring Security 0.11.2.RELEASE**: Spring Security 통합

### Frontend (`/bio_front`)

- **Next.js 14**: React 기반 프레임워크 (App Router)
- **TypeScript**: 타입 안정성
- **React 18**: UI 라이브러리
- **WebAuthn API**: 브라우저 네이티브 생체 인증 지원

### Testing

- **JUnit 5**: 단위 테스트
- **Mockito**: 모킹 프레임워크
- **Spring Boot Test**: 통합 테스트

## 프로젝트 구조

```
passyKey_bio/
├── backend/                 # Spring Boot 백엔드
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/sleekydz86/passykey/
│   │   │   │       ├── adapter/          # 어댑터 레이어
│   │   │   │       │   ├── inbound/      # REST API 컨트롤러
│   │   │   │       │   └── outbound/     # 데이터베이스, Redis 어댑터
│   │   │   │       ├── application/      # 애플리케이션 레이어
│   │   │   │       │   ├── dto/          # 데이터 전송 객체
│   │   │   │       │   └── usecase/      # 유스케이스 구현
│   │   │   │       ├── domain/           # 도메인 레이어
│   │   │   │       │   ├── model/        # 도메인 모델
│   │   │   │       │   ├── port/         # 포트 인터페이스
│   │   │   │       │   └── service/      # 도메인 서비스
│   │   │   │       └── global/           # 전역 설정
│   │   │   └── resources/
│   │   │       ├── application.yml       # 애플리케이션 설정
│   │   │       ├── schema.sql            # 데이터베이스 스키마
│   │   │       └── mapper/               # MyBatis 매퍼 XML
│   │   └── test/                          # 테스트 코드
│   ├── build.gradle.kts                   # Gradle 빌드 설정
│   └── README.md                          # 백엔드 상세 문서
│
└── bio_front/              # Next.js 프론트엔드
    ├── app/                # Next.js App Router 페이지
    │   ├── page.tsx        # 홈 페이지
    │   ├── login/          # 로그인 페이지
    │   ├── register/       # 회원가입 페이지
    │   ├── dashboard/      # 대시보드 페이지
    │   └── layout.tsx      # 루트 레이아웃
    ├── components/         # 재사용 가능한 컴포넌트
    ├── lib/                # 유틸리티 및 API 클라이언트
    ├── types/              # TypeScript 타입 정의
    ├── package.json        # npm 패키지 설정
    └── README.md           # 프론트엔드 상세 문서
```

## 시작하기

### 사전 요구사항

- **Java 21** 이상
- **Node.js 18** 이상 (또는 pnpm)
- **Gradle 8.0** 이상
- **MySQL 8.0** 이상
- **Redis 6.0** 이상

### 설치 및 실행

#### 1. 저장소 클론

```bash
git clone <repository-url>
cd passyKey_bio
```

#### 2. 데이터베이스 설정

MySQL 데이터베이스를 생성하고 설정합니다:

```sql
CREATE DATABASE passkey CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

`backend/src/main/resources/application.yml` 파일에서 데이터베이스 연결 정보를 수정합니다:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/passkey?useSSL=false&serverTimezone=Asia/Seoul
    username: your_username
    password: your_password
```

#### 3. Redis 설정

Redis 서버를 실행하고 `backend/src/main/resources/application.yml` 파일에서 Redis 연결 정보를 수정합니다:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_password # 비밀번호가 있는 경우
```

#### 4. 백엔드 실행

```bash
cd backend

# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun

# 또는 빌드 후 실행
gradlew.bat build  # Windows
./gradlew build    # Linux/Mac
java -jar build/libs/passyKey-0.0.1-SNAPSHOT.jar
```

백엔드는 `http://localhost:8080`에서 실행됩니다.

#### 5. 프론트엔드 실행

새 터미널을 열고:

```bash
cd bio_front

# pnpm 사용 (권장)
pnpm install
pnpm dev

# 또는 npm 사용
npm install
npm run dev
```

프론트엔드는 `http://localhost:80` (또는 `http://localhost:3000`)에서 실행됩니다.

#### 6. 애플리케이션 접속

브라우저에서 `http://localhost`를 열어 애플리케이션을 확인합니다.

## 아키텍처

이 프로젝트는 **Hexagonal Architecture (포트/어댑터 패턴)**를 따릅니다.

```
┌─────────────────────────────────────────────────┐
│              Adapter Layer                       │
│  ┌──────────────┐  ┌─────────────────────────┐ │
│  │ Inbound      │  │ Outbound                │ │
│  │ - Web        │  │ - Persistence (MyBatis) │ │
│  │   Controller │  │ - Service (Redis)       │ │
│  │              │  │ - WebAuthn              │ │
│  └──────────────┘  └─────────────────────────┘ │
└─────────────────────────────────────────────────┘
                      ↕
┌─────────────────────────────────────────────────┐
│            Application Layer                     │
│  ┌──────────────────────────────────────────┐   │
│  │ UseCase Implementation                   │   │
│  │ - UserUseCaseImpl                       │   │
│  │ - WebAuthnRegistrationUseCaseImpl      │   │
│  │ - WebAuthnAuthenticationUseCaseImpl    │   │
│  │ - CredentialManagementUseCaseImpl      │   │
│  └──────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
                      ↕
┌─────────────────────────────────────────────────┐
│              Domain Layer                       │
│  ┌──────────────┐  ┌─────────────────────────┐ │
│  │ Model        │  │ Port (Interface)        │ │
│  │ - User       │  │ - Inbound Port          │ │
│  │ - Credential │  │ - Outbound Port         │ │
│  └──────────────┘  └─────────────────────────┘ │
│  ┌──────────────────────────────────────────┐   │
│  │ Domain Service                           │   │
│  │ - CredentialDomainService               │   │
│  └──────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
```

### 레이어 설명

- **Adapter Layer**: 외부 세계와의 인터페이스 (REST API, 데이터베이스, Redis)
- **Application Layer**: 비즈니스 로직 구현 (UseCase)
- **Domain Layer**: 핵심 비즈니스 규칙과 모델

## API 엔드포인트

### 공개 API (`/api/public`)

#### 사용자 등록

```http
POST /api/public/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com",
  "displayName": "Test User"
}
```

#### 사용자명/이메일 중복 확인

```http
GET /api/public/check-username?username=testuser
GET /api/public/check-email?email=test@example.com
```

### 인증 API (`/api/auth`)

#### 패스키 인증

```http
POST /api/auth/webauthn/authenticate
Content-Type: application/json

{
  "id": "credentialId",
  "rawId": "rawCredentialId",
  "response": {
    "authenticatorData": "base64url...",
    "clientDataJSON": "base64url...",
    "signature": "base64url...",
    "userHandle": "base64url..."
  }
}
```

#### 로그아웃

```http
POST /api/auth/logout
```

### WebAuthn API (`/api/webauthn`)

#### 패스키 등록 옵션 생성

```http
POST /api/webauthn/register/options
```

#### 패스키 등록

```http
POST /api/webauthn/register
Content-Type: application/json

{
  "publicKey": {
    "credential": {
      "id": "credentialId",
      "rawId": "rawCredentialId",
      "type": "public-key",
      "response": {
        "attestationObject": "base64url...",
        "clientDataJSON": "base64url...",
        "transports": ["usb", "nfc"]
      }
    },
    "label": "My Passkey"
  }
}
```

#### 인증 옵션 생성

```http
POST /api/webauthn/authenticate/options?username=testuser
```

#### 인증서 목록 조회

```http
GET /api/webauthn/credentials
```

#### 인증서 삭제

```http
DELETE /api/webauthn/credentials/{credentialId}
```

> **참고**: 더 자세한 API 문서는 `backend/README.md`를 참조하세요.

## 보안

- **BCrypt**: 비밀번호 해싱
- **WebAuthn**: 공개키 암호화 기반 인증
- **세션 기반 챌린지**: Redis를 통한 안전한 챌린지 관리
- **카운터 검증**: 리플레이 공격 방지
- **Spring Security**: 세션 기반 인증 및 권한 관리
- **Base64 URL 인코딩**: WebAuthn 데이터 안전한 전송
- **CORS 설정**: 허용된 Origin만 접근 가능
- **XSS 방지**: 보안 헤더 설정

## 모바일 생체 인증

시스템은 자동으로 디바이스를 감지하여 모바일과 PC에서 다른 인증 방식을 제공합니다.

### 모바일 디바이스 (Android/iOS)

- 기기 내장 생체 인증기 사용 (`authenticatorAttachment: "platform"`)
- 생체 인증 필수 (`userVerification: "required"`)
- 지문 또는 얼굴 인식으로 로그인/등록

### PC 디바이스

- 기존 WebAuthn 방식 유지
- USB 보안 키 또는 기타 외부 인증기 지원

### 지원 환경

- **Windows Hello** (얼굴/지문) → Chrome, Edge, Firefox
- **macOS Touch ID** → Safari, Chrome
- **Android 지문/얼굴** → Chrome
- **iOS Face ID/Touch ID** → Safari

### 주의사항

1. **HTTPS 필수**: localhost 외에는 반드시 HTTPS 환경이어야 합니다
2. **기기 의존**: 사용자 기기에 생체 인증 하드웨어가 있어야 합니다
3. **Fallback 필요**: 생체 인증 미지원 환경을 위한 대체 로그인 수단(비밀번호) 준비
4. **브라우저 지원**: 최신 브라우저 사용 필요

## 데이터베이스

### 테이블 구조

#### users 테이블

- 사용자 정보를 저장하는 테이블
- 주요 필드:
  - `id`: BIGINT (PK, AUTO_INCREMENT)
  - `username`: VARCHAR(50) (UNIQUE, NOT NULL)
  - `password`: VARCHAR(255) (NOT NULL, BCrypt 해시)
  - `email`: VARCHAR(100) (NOT NULL)
  - `display_name`: VARCHAR(100) (NOT NULL)
  - `user_handle`: VARCHAR(255) (NOT NULL, Base64 URL 인코딩)

#### webauthn_credentials 테이블

- WebAuthn 인증서 정보를 저장하는 테이블
- 주요 필드:
  - `id`: BIGINT (PK, AUTO_INCREMENT)
  - `credential_id`: VARCHAR(500) (UNIQUE, NOT NULL, Base64 URL 인코딩)
  - `public_key_cose`: VARCHAR(2000) (NOT NULL, Base64 URL 인코딩)
  - `counter`: BIGINT (NOT NULL, 리플레이 공격 방지용)
  - `transports`: VARCHAR(255) (NOT NULL, 쉼표로 구분: usb,nfc,ble,internal)
  - `label`: VARCHAR(100) (사용자 지정 인증서 이름)
  - `user_id`: BIGINT (FK, users.id 참조, CASCADE DELETE)
  - `created_at`: TIMESTAMP
  - `last_used_at`: TIMESTAMP

### 스키마 초기화

애플리케이션 시작 시 `backend/src/main/resources/schema.sql` 파일이 자동으로 실행되어 테이블, 뷰, 저장 프로시저가 생성됩니다.

## 테스트

### 백엔드 테스트 실행

```bash
cd backend

# Windows
gradlew.bat test

# Linux/Mac
./gradlew test

# 특정 테스트 클래스 실행
gradlew.bat test --tests "UserControllerTest"
```

테스트 리포트는 `backend/build/reports/tests/test/index.html`에서 확인할 수 있습니다.

## 문제 해결

### 일반적인 문제

#### 1. Redis 연결 실패

**증상**: `Connection refused` 또는 `Unable to connect to Redis`

**해결책**:

- Redis 서버가 실행 중인지 확인
- `application.yml`의 Redis 호스트와 포트 확인
- 방화벽 설정 확인

#### 2. MySQL 연결 실패

**증상**: `Access denied` 또는 `Connection refused`

**해결책**:

- MySQL 서버가 실행 중인지 확인
- 데이터베이스가 생성되었는지 확인
- `application.yml`의 데이터베이스 연결 정보 확인
- 사용자 권한 확인

#### 3. 패스키 등록 실패

**증상**: "이 브라우저는 생체 인증을 지원하지 않습니다"

**해결책**:

- 최신 브라우저 사용 (Chrome, Safari, Edge, Firefox)
- HTTPS 환경에서 테스트 (localhost는 예외)
- 기기에 생체 인증 하드웨어가 있는지 확인

#### 4. CORS 오류

**증상**: `CORS policy` 오류

**해결책**:

- `application.yml`의 `webauthn.allowed.origins`에 프론트엔드 URL 추가
- 프론트엔드와 백엔드의 Origin이 일치하는지 확인

#### 5. 프론트엔드에서 API 호출 실패

**증상**: `404 Not Found` 또는 `Network Error`

**해결책**:

- 백엔드가 `http://localhost:8080`에서 실행 중인지 확인
- `next.config.js`의 `rewrites` 설정 확인
- 브라우저 개발자 도구의 Network 탭에서 실제 요청 URL 확인

## 추가 문서

- [백엔드 상세 문서](./backend/README.md)
- [프론트엔드 상세 문서](./bio_front/README.md)
- [WebAuthn 구현 가이드](https://velog.io/@sleekydevzero86/spring-webauthn-passkeys-implementation)

## 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

## 기여

이슈 및 풀 리퀘스트를 환영합니다!

## 문의

프로젝트에 대한 문의사항이 있으시면 이슈를 생성해주세요.
