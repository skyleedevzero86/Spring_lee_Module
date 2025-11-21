# 실시간 번역 서비스 (Real-time Translation Service)

> DeepL API 기반 고품질 번역 웹 애플리케이션

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 프로젝트 소개

DeepL API를 활용한 실시간 번역 웹 애플리케이션입니다. DDD(Domain-Driven Design)와 헥사고날 아키텍처를 적용하여 확장 가능하고 유지보수가 용이한 구조로 설계했습니다.

### 주요 기능
- 실시간 텍스트 번역 (14개 언어 지원)
- 자동 언어 감지
- API 사용량 실시간 모니터링
- 반응형 웹 디자인
- REST API 제공

---

## 프로젝트 목표

### 1. 아키텍처 설계 역량 증명
- DDD 및 헥사고날 아키텍처 실무 적용
- 도메인 중심 설계로 비즈니스 로직 분리
- SOLID 원칙 준수

### 2. 확장 가능한 설계
- 다른 번역 API로 쉽게 교체 가능한 구조
- 테스트 용이성 (포트 인터페이스 활용)
- 관심사의 명확한 분리

### 3. 실무 수준의 코드 품질
- 불변 객체 패턴 적용
- 전역 예외 처리
- 명확한 로깅 전략

---

## 기술 스택

### Backend
- **Java 21** - LTS 버전
- **Spring Boot 3.5.8** - 웹 프레임워크
- **Thymeleaf** - 템플릿 엔진
- **Spring Validation** - 입력 검증
- **DeepL Java SDK 1.5.0** - 번역 API 클라이언트


---

## 아키텍처

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Thymeleaf View / REST Controller)     │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│        Application Layer                │
│  (Use Cases / Application Services)     │
│         (DTO Conversion)                │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│          Domain Layer                   │
│    (Business Logic / Entities)          │
│   (Value Objects / Domain Services)     │
│         (Port Interfaces)               │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│      Infrastructure Layer               │
│     (DeepL Adapter / Config)            │
│      (Exception Handling)               │
└─────────────────────────────────────────┘
```

### 계층별 책임

#### 1. Domain Layer (핵심)
- 비즈니스 로직
- 도메인 모델 (Entity, Value Object)
- 포트 인터페이스 정의
- 외부 의존성 없음

#### 2. Application Layer
- 유스케이스 조율
- DTO 변환 (Domain ↔ DTO)
- 트랜잭션 관리

#### 3. Adapter Layer
- 외부 시스템과의 통신
- 웹 요청 처리 (Controller)
- API 클라이언트 구현

#### 4. Infrastructure Layer
- 기술적 구현
- 설정 관리
- 전역 예외 처리

---

## 디렉토리 구조
```
src/main/java/com/sleekydz86/tran/
├── domain/
│   ├── model/
│   │   ├── record/                    # 도메인 모델 (불변 객체)
│   │   │   ├── Language.java         # 언어 Value Object
│   │   │   ├── Translation.java       # 번역 결과 Entity
│   │   │   └── TranslationRequest.java # 번역 요청 Value Object
│   │   └── port/
│   │       └── out/
│   │           └── TranslationPort.java # 외부 포트 인터페이스
│   └── application/
│       ├── usecase/
│       │   └── TranslateTextUseCase.java # 번역 유스케이스
│       └── dto/
│           ├── TranslationRequestDto.java
│           └── TranslationResponseDto.java
├── adapter/
│   ├── in/
│   │   └── web/
│   │       └── ViewController.java    # 웹 컨트롤러
│   └── out/
│       └── translation/
│           └── DeepLTranslationAdapter.java # DeepL API 어댑터
└── global/
    ├── config/
    │   └── DeepLConfig.java           # DeepL 설정
    └── exception/
        ├── TranslationException.java  # 도메인 예외
        └── GlobalExceptionHandler.java # 전역 예외 핸들러

src/main/resources/
├── templates/
│   └── index.html                     # Thymeleaf 템플릿
└── application.yml                    # 애플리케이션 설정
```

---

## 실행 방법

### 사전 요구사항
- JDK 21 이상
- Gradle 8.x



### 설정

**application.yml 방식**
```yaml
deepl:
  api:
    key: your-api-key-here
    base-url: https://api-free.deepl.com
    enabled: true
```

**환경 변수 방식 (권장)**
```bash
# Linux/Mac
export DEEPL_API_KEY="your-api-key-here"

# Windows (PowerShell)
$env:DEEPL_API_KEY="your-api-key-here"
```

### 빌드 및 실행
```bash
# 프로젝트 클론
git clone https://github.com/yourusername/translation-service.git
cd translation-service

# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

### 접속
```
http://localhost:8080
```

---

## 핵심 설계 원칙

### 1. 도메인 중심 설계

**불변 Value Object**
```java
public record Language(String code, String displayName) {
    public static Language of(String code) {
        validateCode(code);
        return new Language(code, getDisplayName(code));
    }
    
    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("언어 코드는 필수입니다");
        }
    }
}
```

**도메인 모델**
```java
public record Translation(
    String originalText,
    String translatedText,
    Language detectedSourceLanguage,
    Language targetLanguage,
    LocalDateTime translatedAt
) {
    public static Translation of(
        String originalText,
        String translatedText,
        Language sourceLanguage,
        Language targetLanguage
    ) {
        validate(originalText, translatedText);
        return new Translation(
            originalText,
            translatedText,
            sourceLanguage,
            targetLanguage,
            LocalDateTime.now()
        );
    }
}
```

### 2. 포트와 어댑터 패턴

**Port (도메인 인터페이스)**
```java
public interface TranslationPort {
    Translation translate(TranslationRequest request);
    boolean isAvailable();
    String getUsageInfo();
}
```

**Adapter (구현체)**
```java
@Component
public class DeepLTranslationAdapter implements TranslationPort {
    private final Translator translator;
    
    @Override
    public Translation translate(TranslationRequest request) {
        // DeepL API 호출 로직
        TextResult result = translator.translateText(
            request.getSourceText(),
            request.getSourceLanguage().getCode(),
            request.getTargetLanguage().getCode()
        );
        
        return Translation.of(
            request.getSourceText(),
            result.getText(),
            Language.of(result.getDetectedSourceLanguage()),
            request.getTargetLanguage()
        );
    }
}
```



---

## 주요 기능

### 1. 실시간 번역
- 14개 언어 지원
    - 한국어, 영어, 일본어, 중국어, 스페인어, 프랑스어, 독일어 등

- 자동 언어 감지 기능

### 2. 사용량 모니터링


### 3. 전역 예외 처리


### 4. 입력 검증




## 참고 자료

### 아키텍처
- [Hexagonal Architecture - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design - Martin Fowler](https://martinfowler.com/tags/domain%20driven%20design.html)

### 기술 문서

- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Thymeleaf Documentation](https://www.thymeleaf.org/documentation.html)

---

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

---



## 프로젝트 배경

이 프로젝트는 단순한 번역 서비스 구현을 넘어, 실무에서 적용 가능한 아키텍처 패턴을 학습하고 구현하기 위해 개발되었습니다. <br/>
특히 DDD와 헥사고날 아키텍처를 실제 프로젝트에 적용하면서, 확장 가능하고 유지보수가 용이한 코드 작성에 중점을 두었습니다.