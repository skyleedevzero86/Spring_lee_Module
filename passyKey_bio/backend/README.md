# PassyKey_bio

WebAuthn/Passkey 기반의 비밀번호 없는 인증 시스템입니다.
Spring Boot와 모바일에서 테스트할수있게 기반으로 구현되었습니다.

## 📋 목차

- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [아키텍처](#아키텍처)
- [프로젝트 구조](#프로젝트-구조)
- [시작하기](#시작하기)
- [API 엔드포인트](#api-엔드포인트)
- [테스트](#테스트)
- [데이터베이스](#데이터베이스)

## ✨ 주요 기능

- **비밀번호 없는 인증**: WebAuthn/Passkey를 통한 강력한 인증
- **사용자 관리**: 사용자 등록, 조회, 중복 확인
- **패스키 관리**: 패스키 등록, 인증, 삭제
- **세션 기반 챌린지 관리**: Redis를 활용한 안전한 챌린지 저장
- **리플레이 공격 방지**: 카운터 기반 인증서 검증

## 🛠 기술 스택

### Backend

- **Java 21**: 최신 Java 기능 활용
- **Spring Boot 3.5.7**: 웹 애플리케이션 프레임워크
- **Spring Security**: 인증 및 보안
- **MyBatis 3.0.3**: SQL 매퍼 프레임워크
- **MySQL 9.1.0**: 관계형 데이터베이스 (MySQL Connector/J 9.1.0)
- **Redis**: 세션 및 챌린지 캐시
- **WebAuthn4j 0.30.0.RELEASE**: WebAuthn 프로토콜 구현
- **WebAuthn4j Spring Security 0.11.2.RELEASE**: Spring Security 통합

### Frontend

- **Next.js 14**: React 기반 프레임워크 (별도 프로젝트: `bio_front`)
- **TypeScript**: 타입 안정성
- **WebAuthn API**: 브라우저 네이티브 생체 인증 지원

### Testing

- **JUnit 5**: 단위 테스트
- **Mockito**: 모킹 프레임워크
- **Spring Boot Test**: 통합 테스트

## 🏗 아키텍처

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

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/
│   │   └── com/sleekydz86/passykey/
│   │       ├── adapter/              # 어댑터 레이어
│   │       │   ├── inbound/          # 인바운드 어댑터
│   │       │   │   └── web/          # REST API 컨트롤러
│   │       │   │       ├── AuthController.java
│   │       │   │       ├── BaseController.java
│   │       │   │       ├── UserController.java
│   │       │   │       └── WebAuthnController.java
│   │       │   └── outbound/         # 아웃바운드 어댑터
│   │       │       ├── persistence/ # MyBatis 매퍼
│   │       │       ├── service/      # Redis 서비스
│   │       │       └── webauthn/     # WebAuthn 어댑터
│   │       ├── application/          # 애플리케이션 레이어
│   │       │   ├── dto/              # 데이터 전송 객체
│   │       │   │   ├── ApiResponse.java
│   │       │   │   ├── AuthenticationResponse.java
│   │       │   │   ├── PasskeyAuthenticationRequest.java
│   │       │   │   ├── PasskeyRegistrationRequest.java
│   │       │   │   └── RegisterRequest.java
│   │       │   └── usecase/         # 유스케이스 구현
│   │       │       ├── CredentialManagementUseCaseImpl.java
│   │       │       ├── UserUseCaseImpl.java
│   │       │       ├── WebAuthnAuthenticationUseCaseImpl.java
│   │       │       └── WebAuthnRegistrationUseCaseImpl.java
│   │       ├── domain/               # 도메인 레이어
│   │       │   ├── model/           # 도메인 모델
│   │       │   │   ├── User.java
│   │       │   │   └── WebAuthnCredential.java
│   │       │   ├── port/            # 포트 인터페이스
│   │       │   │   ├── inbound/     # 인바운드 포트
│   │       │   │   └── outbound/    # 아웃바운드 포트
│   │       │   └── service/         # 도메인 서비스
│   │       │       └── CredentialDomainService.java
│   │       └── global/              # 전역 설정
│   │           ├── config/         # 설정 클래스
│   │           ├── constants/      # 상수 정의
│   │           ├── exception/       # 예외 처리
│   │           ├── security/       # 보안 설정
│   │           └── util/           # 유틸리티
│   └── resources/
│       ├── application.yml         # 애플리케이션 설정
│       ├── schema.sql              # 데이터베이스 스키마
│       └── mapper/                 # MyBatis 매퍼 XML
└── test/
    └── java/                       # 테스트 코드
```

## 🚀 시작하기

### 사전 요구사항

- Java 21 이상
- Gradle 8.0 이상
- MySQL 8.0 이상
- Redis 6.0 이상

### 설치 및 실행

1. **저장소 클론**

   ```bash
   git clone <repository-url>
   cd passyKey
   ```

2. **데이터베이스 설정**

   - MySQL 데이터베이스 생성

   ```sql
   CREATE DATABASE passykeydb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

   - `src/main/resources/application.yml` 파일에서 데이터베이스 연결 정보 수정

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/passykeydb
       username: your_username
       password: your_password
   ```

3. **Redis 설정**

   - Redis 서버 실행
   - `src/main/resources/application.yml` 파일에서 Redis 연결 정보 수정

   ```yaml
   spring:
     data:
       redis:
         host: 59.3.21.121
         port: 9379
         password: 궁금하면500원
   ```

4. **애플리케이션 실행**

   ```bash
   # Windows
   gradlew.bat bootRun

   # Linux/Mac
   ./gradlew bootRun

   # 또는 빌드 후 실행
   gradlew.bat build  # Windows
   ./gradlew build    # Linux/Mac
   java -jar build/libs/passyKey-0.0.1-SNAPSHOT.jar
   ```

5. **프론트엔드 실행**

   - 별도 프로젝트 `bio_front` 디렉토리에서 실행
   - 자세한 내용은 `bio_front/README.md` 참조

6. **애플리케이션 접속**
   - 프론트엔드: `http://localhost` (포트 80)
   - 백엔드 API: `http://localhost:8080`

## 📡 API 엔드포인트

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

**응답 예시:**

```json
{
  "success": true,
  "message": "사용자 등록 성공",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "displayName": "Test User",
    "userHandle": "base64url..."
  }
}
```

#### 사용자명 중복 확인

```http
GET /api/public/check-username?username=testuser
```

**응답 예시:**

```json
{
  "success": true,
  "message": "사용자명 확인 완료",
  "data": false
}
```

#### 이메일 중복 확인

```http
GET /api/public/check-email?email=test@example.com
```

**응답 예시:**

```json
{
  "success": true,
  "message": "이메일 확인 완료",
  "data": false
}
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

**응답 예시:**

```json
{
  "success": true,
  "message": "인증 성공",
  "data": {
    "redirectUrl": "/dashboard",
    "authenticated": true
  }
}
```

#### 로그아웃

```http
POST /api/auth/logout
```

**응답 예시:**

```json
{
  "success": true,
  "message": "로그아웃 성공",
  "data": null
}
```

### WebAuthn API (`/api/webauthn`)

#### 패스키 등록 옵션 생성

```http
POST /api/webauthn/register/options
```

> **참고**: 세션 기반 인증이 필요합니다. 로그인 후 사용 가능합니다.

**응답 예시:**

```json
{
  "success": true,
  "message": "등록 옵션 생성 완료",
  "data": {
    "rp": {
      "name": "PassyKey Application",
      "id": "localhost"
    },
    "user": {
      "id": "base64url...",
      "name": "testuser",
      "displayName": "Test User"
    },
    "challenge": "base64url...",
    "pubKeyCredParams": [...],
    "timeout": 60000,
    "authenticatorSelection": {...}
  }
}
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

**응답 예시:**

```json
{
  "success": true,
  "message": "인증서 등록 성공",
  "data": {
    "success": true
  }
}
```

#### 인증 옵션 생성

```http
POST /api/webauthn/authenticate/options?username=testuser
```

> **참고**: `username` 파라미터는 선택사항입니다. 세션에 로그인된 사용자가 있으면 해당 사용자의 인증 옵션이 생성됩니다.

**응답 예시:**

```json
{
  "success": true,
  "message": "인증 옵션 생성 완료",
  "data": {
    "challenge": "base64url...",
    "timeout": 60000,
    "rpId": "localhost",
    "allowCredentials": [...],
    "userVerification": "preferred"
  }
}
```

#### 인증서 목록 조회

```http
GET /api/webauthn/credentials
```

> **참고**: 세션 기반 인증이 필요합니다.

**응답 예시:**

```json
{
  "success": true,
  "message": "인증서 조회 완료",
  "data": [
    {
      "id": 1,
      "credentialId": "base64url...",
      "counter": 5,
      "transports": "usb,nfc",
      "label": "My Passkey",
      "createdAt": "2025-01-01T00:00:00",
      "lastUsedAt": "2025-01-15T12:30:00"
    }
  ]
}
```

#### 인증서 삭제

```http
DELETE /api/webauthn/credentials/{credentialId}
```

> **참고**: 세션 기반 인증이 필요합니다. `credentialId`는 Base64 URL 인코딩된 값입니다.

**응답 예시:**

```json
{
  "success": true,
  "message": "인증서 삭제 성공",
  "data": null
}
```

## 🧪 테스트

### 테스트 실행

```bash
# Windows
# 모든 테스트 실행
gradlew.bat test

# 특정 테스트 클래스 실행
gradlew.bat test --tests "UserControllerTest"

# Linux/Mac
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "UserControllerTest"

# 테스트 리포트 확인
# 리포트 위치: build/reports/tests/test/index.html
```

### 테스트 커버리지

프로젝트에는 다음 테스트가 포함되어 있습니다:

- **Controller 테스트**: REST API 엔드포인트 테스트
- **UseCase 테스트**: 비즈니스 로직 테스트
- **Domain Service 테스트**: 도메인 서비스 테스트
- **Domain Model 테스트**: 도메인 모델 테스트

## 🗄 데이터베이스

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
  - `enabled`, `account_non_expired`, `account_non_locked`, `credentials_non_expired`: BOOLEAN

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

### 뷰 및 저장 프로시저

스키마에는 다음이 포함되어 있습니다:

- `v_users`: 사용자 정보 조회 뷰
- `v_webauthn_credentials`: 인증서와 사용자 정보 조인 뷰
- `sp_save_user`: 사용자 정보 저장 프로시저 (INSERT/UPDATE 통합)
- `sp_save_webauthn_credential`: 인증서 정보 저장 프로시저 (INSERT/UPDATE 통합)

### 스키마 초기화

애플리케이션 시작 시 `src/main/resources/schema.sql` 파일이 자동으로 실행되어 테이블, 뷰, 저장 프로시저가 생성됩니다.

## 🔒 보안

- **BCrypt**: 비밀번호 해싱
- **WebAuthn**: 공개키 암호화 기반 인증
- **세션 기반 챌린지**: Redis를 통한 안전한 챌린지 관리
- **카운터 검증**: 리플레이 공격 방지
- **Spring Security**: 세션 기반 인증 및 권한 관리
- **Base64 URL 인코딩**: WebAuthn 데이터 안전한 전송

## 📝 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

## 👥 기여

이슈 및 풀 리퀘스트를 환영합니다!

## 📧 문의

프로젝트에 대한 문의사항이 있으시면 이슈를 생성해주세요.
<br/>

<a href="https://velog.io/@sleekydevzero86/spring-webauthn-passkeys-implementation">비밀번호 없는 인증, Spring Boot 서버 측 검증 로직 적용하기</a>
