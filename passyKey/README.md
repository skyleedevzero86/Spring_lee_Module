# PassyKey

WebAuthn/Passkey 기반의 비밀번호 없는 인증 시스템입니다. Spring Boot와 Hexagonal Architecture를 기반으로 구현되었습니다.

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
- **MySQL 9.0**: 관계형 데이터베이스
- **Redis**: 세션 및 챌린지 캐시
- **WebAuthn4j 0.29.7**: WebAuthn 프로토콜 구현

### Frontend
- **Thymeleaf**: 서버 사이드 템플릿 엔진
- **HTML/CSS/JavaScript**: 클라이언트 사이드 구현

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
│   │       │   └── outbound/         # 아웃바운드 어댑터
│   │       │       ├── persistence/ # MyBatis 매퍼
│   │       │       ├── service/      # Redis 서비스
│   │       │       └── webauthn/     # WebAuthn 어댑터
│   │       ├── application/          # 애플리케이션 레이어
│   │       │   ├── dto/              # 데이터 전송 객체
│   │       │   └── usecase/         # 유스케이스 구현
│   │       ├── domain/               # 도메인 레이어
│   │       │   ├── model/           # 도메인 모델
│   │       │   ├── port/            # 포트 인터페이스
│   │       │   └── service/         # 도메인 서비스
│   │       └── global/              # 전역 설정
│   │           ├── config/         # 설정 클래스
│   │           ├── exception/       # 예외 처리
│   │           ├── security/       # 보안 설정
│   │           └── util/           # 유틸리티
│   └── resources/
│       ├── application.yml         # 애플리케이션 설정
│       ├── schema.sql              # 데이터베이스 스키마
│       ├── mapper/                 # MyBatis 매퍼 XML
│       ├── templates/              # Thymeleaf 템플릿
│       └── static/                  # 정적 리소스
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
         host: localhost
         port: 6379
         password: your_password
   ```

4. **애플리케이션 실행**
   ```bash
   # Gradle Wrapper 사용
   ./gradlew bootRun
   
   # 또는 빌드 후 실행
   ./gradlew build
   java -jar build/libs/passyKey-0.0.1-SNAPSHOT.jar
   ```

5. **애플리케이션 접속**
   - 웹 브라우저에서 `http://localhost:8080` 접속

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

#### 사용자명 중복 확인
```http
GET /api/public/check-username?username=testuser
```

#### 이메일 중복 확인
```http
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
Authorization: Bearer <token>
```

#### 패스키 등록
```http
POST /api/webauthn/register
Content-Type: application/json
Authorization: Bearer <token>

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
Authorization: Bearer <token>
```

#### 인증서 삭제
```http
DELETE /api/webauthn/credentials/{credentialId}
Authorization: Bearer <token>
```

## 🧪 테스트

### 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "UserControllerTest"

# 테스트 리포트 확인
./gradlew test
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
- 주요 필드: `id`, `username`, `password`, `email`, `display_name`, `user_handle`

#### webauthn_credentials 테이블
- WebAuthn 인증서 정보를 저장하는 테이블
- 주요 필드: `id`, `credential_id`, `public_key_cose`, `counter`, `transports`, `user_id`

### 스키마 초기화

애플리케이션 시작 시 `src/main/resources/schema.sql` 파일이 자동으로 실행되어 테이블이 생성됩니다.

## 🔒 보안

- **BCrypt**: 비밀번호 해싱
- **WebAuthn**: 공개키 암호화 기반 인증
- **세션 기반 챌린지**: Redis를 통한 안전한 챌린지 관리
- **카운터 검증**: 리플레이 공격 방지

## 📝 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

## 👥 기여

이슈 및 풀 리퀘스트를 환영합니다!

## 📧 문의

프로젝트에 대한 문의사항이 있으시면 이슈를 생성해주세요.
<br/>

<a href="https://velog.io/@sleekydevzero86/spring-webauthn-passkeys-implementation">비밀번호 없는 인증, Spring Boot 서버 측 검증 로직 적용하기</a>

