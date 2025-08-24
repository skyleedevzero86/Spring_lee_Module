# 🔐 Java OTP (One-Time Password) 시스템

Spring Boot 기반의 2단계 인증(2FA) 시스템으로, TOTP(Time-based One-Time Password) 알고리즘을 사용하여 보안을 강화합니다.

## ✨ 주요 기능

- **🔐 2단계 인증**: TOTP 기반 보안 강화
- **📱 QR 코드 스캔**: Google Authenticator, Microsoft Authenticator, Authy 등 호환
- **🔑 시크릿 키 수동 입력**: QR 코드 스캔이 어려울 때 대안 제공
- **👤 사용자 관리**: 회원가입, 로그인, OTP 설정
- **🎨 모던 UI**: 반응형 웹 디자인과 직관적인 사용자 경험
- **🛡️ Spring Security**: 강력한 보안 프레임워크 적용

## 🚀 기술 스택

- **Backend**: Spring Boot 3.5.4, Java 21
- **Database**: H2 Database (In-Memory)
- **Security**: Spring Security, BCrypt 암호화
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
- **OTP**: TOTP 알고리즘, HMAC-SHA256, ZXing QR 코드
- **Build Tool**: Gradle (Kotlin DSL)

## 📋 요구사항

- Java 21 이상
- Gradle 8.0 이상
- 최소 4GB RAM

## 🛠️ 설치 및 실행

### 1. 프로젝트 클론

```bash
git clone <repository-url>
cd java_otp
```

### 2. 애플리케이션 실행

```bash
# Gradle Wrapper 사용
./gradlew bootRun

# 또는 직접 실행
./gradlew build
java -jar build/libs/java_otp-0.0.1-SNAPSHOT.jar
```

### 3. 접속

- **메인 페이지**: http://localhost:8080
- **H2 콘솔**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (비어있음)

## 🎯 사용 방법

### 1. 회원가입

1. 메인 페이지에서 "회원가입" 클릭
2. 사용자명과 비밀번호 입력
3. 회원가입 완료

### 2. OTP 설정

1. 로그인 후 대시보드에서 "OTP 설정하기" 클릭
2. OTP 앱 설치 (Google Authenticator, Microsoft Authenticator, Authy)
3. QR 코드 스캔 또는 시크릿 키 수동 입력
4. 6자리 OTP 코드 입력하여 활성화

### 3. 2단계 인증 로그인

1. 사용자명과 비밀번호 입력
2. OTP 앱에서 생성된 6자리 코드 입력
3. 로그인 완료

## 🏗️ 프로젝트 구조

```
src/main/java/com/sleekydz86/
├── JavaOtpApplication.java          # 메인 애플리케이션 클래스
├── domain/
│   ├── controller/                  # REST API 및 웹 컨트롤러
│   │   ├── UserController.java     # 사용자 관련 API
│   │   └── WebController.java      # 웹 페이지 컨트롤러
│   ├── dto/                        # 데이터 전송 객체
│   │   ├── LoginRequest.java       # 로그인 요청
│   │   ├── LoginResponse.java      # 로그인 응답
│   │   ├── OtpLoginRequest.java    # OTP 로그인 요청
│   │   ├── OtpSetupResponse.java   # OTP 설정 응답
│   │   ├── OtpVerifyRequest.java   # OTP 검증 요청
│   │   ├── OtpVerifyResponse.java  # OTP 검증 응답
│   │   ├── RegisterRequest.java    # 회원가입 요청
│   │   └── RegisterResponse.java   # 회원가입 응답
│   ├── entity/                     # 엔티티 클래스
│   │   └── User.java              # 사용자 엔티티
│   ├── repository/                 # 데이터 접근 계층
│   │   └── UserRepository.java    # 사용자 저장소
│   └── service/                    # 비즈니스 로직
│       ├── OtpService.java        # OTP 관련 서비스
│       └── UserService.java       # 사용자 관련 서비스
├── global/
│   └── config/                     # 설정 클래스
│       ├── DataInitializer.java   # 초기 데이터 설정
│       └── SecurityConfig.java    # 보안 설정
└── resources/
    ├── templates/                  # Thymeleaf 템플릿
    │   ├── dashboard.html         # 대시보드
    │   ├── index.html             # 메인 페이지
    │   ├── login.html             # 로그인 페이지
    │   ├── otp-setup.html         # OTP 설정 페이지
    │   ├── otp-verify.html        # OTP 검증 페이지
    │   └── register.html          # 회원가입 페이지
    └── application.yml            # 애플리케이션 설정
```

## ⚙️ 설정

### application.yml 주요 설정

```yaml
otp:
  issuer: MyApplication # OTP 발급자명
  algorithm: HmacSHA256 # 해시 알고리즘
  digits: 6 # OTP 자릿수
  period: 30 # OTP 유효 시간(초)
  window: 1 # 시간 윈도우
```

### 보안 설정

- CSRF 비활성화 (개발 환경)
- 세션 기반 인증
- H2 콘솔 접근 허용

## 🔒 보안 특징

- **BCrypt 암호화**: 비밀번호 해시 저장
- **TOTP 알고리즘**: 시간 기반 일회용 비밀번호
- **HMAC-SHA256**: 강력한 해시 알고리즘
- **세션 관리**: 안전한 사용자 세션 처리

## 📱 OTP 앱 호환성

- ✅ Google Authenticator
- ✅ Microsoft Authenticator
- ✅ Authy
- ✅ 기타 TOTP 표준 지원 앱

## 🧪 테스트

### 테스트 사용자

- **Username**: `testuser`
- **Password**: `password123`

### 테스트 방법

```bash
# 단위 테스트 실행
./gradlew test

# 통합 테스트 실행
./gradlew integrationTest
```

## 🐛 문제 해결

### QR 코드 스캔이 안 될 때

1. **화면 밝기**: 모니터 밝기를 최대로 설정
2. **거리 조정**: 카메라와 QR 코드 사이 거리 15-25cm 유지
3. **안정성**: QR 코드가 흔들리지 않도록 고정
4. **수동 입력**: 시크릿 키를 복사하여 OTP 앱에 수동 입력

### 일반적인 문제

- **포트 충돌**: `application.yml`에서 포트 변경
- **데이터베이스**: H2 콘솔에서 데이터 확인
- **로그**: 콘솔 출력 확인

## 📈 성능 최적화

- **QR 코드 크기**: 400x400 픽셀로 최적화
- **이미지 압축**: PNG 형식으로 품질 유지
- **캐싱**: Thymeleaf 캐시 비활성화 (개발 환경)

## 🔄 API 엔드포인트

### 인증 API

- `POST /api/auth/register` - 회원가입
- `POST /api/auth/login` - 로그인
- `POST /api/auth/otp/verify-login` - OTP 로그인 검증
- `POST /api/auth/otp/setup/{username}` - OTP 설정
- `POST /api/auth/otp/verify-setup` - OTP 설정 검증
- `POST /api/auth/otp/disable` - OTP 비활성화

### 웹 페이지

- `GET /` - 메인 페이지
- `GET /login` - 로그인 페이지
- `GET /register` - 회원가입 페이지
- `GET /dashboard` - 대시보드
- `GET /otp-setup` - OTP 설정 페이지
- `GET /otp-verify` - OTP 검증 페이지

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 📞 지원

문제가 발생하거나 질문이 있으시면:

- Issue 생성
- 이메일: [sleekydz86@naver.com]
- 프로젝트 위키 참조

**⭐ 이 프로젝트가 도움이 되었다면 스타를 눌러주세요!**
