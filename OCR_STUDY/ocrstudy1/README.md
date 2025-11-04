# OCR Study 01

> **AI 기반 지능형 문서 분석 플랫폼**

OCR Study 01은 이미지 업로드부터 OCR 텍스트 추출, 얼굴 인식, 문서 타입 감지, 신분증 검증, AI 기반 문서 분석까지 **전체 문서 처리 파이프라인**을 제공하는 Spring Boot 기반 엔터프라이즈 애플리케이션입니다.

---

## 🎯 프로젝트 소개

OCR Study 01은 **실제 서비스 환경에서 사용 가능한 수준의 문서 처리 시스템**을 구현한 프로젝트입니다. 단순한 OCR 기능을 넘어서, 다음과 같은 **엔드투엔드 문서 처리 워크플로우**를 제공합니다:

1. **이미지 업로드 및 저장** (MinIO)
2. **OCR 텍스트 추출** (Tesseract - 한글/영문 지원)
3. **얼굴 인식 및 인코딩** (DJL + PyTorch)
4. **문서 타입 자동 감지** (영수증, 신분증, 운전면허증, 여권, 세금계산서, 계약서, 증명서 등)
5. **신분증 정보 추출 및 검증** (정규식 기반 패턴 매칭)
6. **AI 기반 문서 분석** (Spring AI + OpenAI)
7. **이미지 간 비교** (얼굴 유사도, OCR 텍스트 유사도)
8. **민감 정보 암호화 저장** (AES-256-GCM)

---

## ✨ 주요 기능

### 1. 이미지 업로드 및 자동 처리

- **비동기 처리**: OCR과 얼굴 인식을 병렬로 처리하여 성능 최적화
- **MinIO 스토리지**: 객체 스토리지에 파일 저장 및 관리
- **자동 메타데이터 추출**: 파일 크기, 타입, 원본 파일명 등 자동 저장

### 2. OCR 텍스트 추출

- **Tesseract OCR**: 한국어와 영어 동시 지원
- **고품질 텍스트 추출**: 다양한 이미지 형식 지원 (JPEG, PNG, BMP, GIF)
- **추출된 텍스트 암호화 저장**: 민감 정보 보호

### 3. 얼굴 인식 및 비교

- **DJL 기반 얼굴 인식**: PyTorch 모델을 활용한 정확한 얼굴 감지
- **얼굴 인코딩 저장**: 얼굴 특징 벡터를 추출하여 저장
- **얼굴 유사도 비교**: 코사인 유사도를 활용한 얼굴 비교 기능

### 4. 문서 타입 자동 감지

- **8가지 문서 타입 지원**:
  - 영수증 (RECEIPT)
  - 주민등록증 (NATIONAL_ID)
  - 운전면허증 (DRIVER_LICENSE)
  - 여권 (PASSPORT)
  - 세금계산서 (TAX_INVOICE)
  - 계약서 (CONTRACT)
  - 증명서 (CERTIFICATE)
  - 기타 (ETC)

### 5. 신분증 검증

- **자동 신분증 감지**: 주민등록증, 운전면허증, 여권 자동 식별
- **정보 추출**: 이름, 주민번호, 생년월일, 주소 등 자동 추출
- **정규식 기반 검증**: 정확한 패턴 매칭을 통한 정보 검증

### 6. AI 기반 문서 분석

- **Spring AI 통합**: OpenAI GPT 모델 활용
- **문서 분석**: 추출된 텍스트를 기반으로 문서 내용 분석
- **진위 검증**: AI를 통한 문서 진위 여부 검증
- **요약 생성**: 긴 문서의 핵심 내용 요약

### 7. 이미지 간 비교

- **얼굴 유사도 비교**: 얼굴 인코딩 벡터 간 코사인 유사도 계산
- **OCR 텍스트 유사도**: Jaccard 유사도를 활용한 텍스트 비교
- **자동 비교 방식 선택**: 얼굴이 있으면 얼굴 유사도, 없으면 텍스트 유사도 사용

### 8. 보안 및 성능

- **민감 정보 암호화**: AES-256-GCM 알고리즘으로 OCR 텍스트 및 신분증 정보 암호화
- **Redis 캐싱**: 문서 분석 결과 캐싱으로 성능 향상
- **비동기 처리**: @Async를 활용한 논블로킹 처리

---

## 🚀 프로젝트 차별점

### 1. **엔터프라이즈급 아키텍처**

- **헥사고날 아키텍처 (Hexagonal Architecture)**: 포트와 어댑터 패턴으로 비즈니스 로직과 인프라 완전 분리
- **DDD (Domain-Driven Design)**: 도메인 중심 설계로 비즈니스 로직의 명확한 표현
- **클린 아키텍처 원칙**: 의존성 역전 원칙(DIP) 준수로 테스트 용이성 및 유지보수성 향상

### 2. **통합 문서 처리 파이프라인**

- 단순 OCR을 넘어서 **문서 타입 감지 → 신분증 검증 → AI 분석**까지 전체 워크플로우 자동화
- **비동기 병렬 처리**: OCR과 얼굴 인식을 동시에 처리하여 처리 시간 단축
- **스마트한 문서 분류**: OCR 텍스트와 파일명을 분석하여 문서 타입 자동 감지

### 3. **보안 중심 설계**

- **민감 정보 암호화**: OCR 텍스트와 신분증 정보를 AES-256-GCM으로 암호화 저장
- **암호화 키 관리**: 환경 변수를 통한 안전한 키 관리
- **데이터 보호**: 민감 정보는 암호화된 상태로만 저장, 복호화는 필요 시에만 수행

### 4. **AI 기반 지능형 분석**

- **Spring AI 통합**: OpenAI GPT 모델을 활용한 문서 분석
- **다양한 분석 기능**: 문서 분석, 진위 검증, 요약 생성 등 다양한 AI 기능 제공
- **캐싱을 통한 비용 최적화**: Redis를 활용한 분석 결과 캐싱으로 API 호출 비용 절감

### 5. **고성능 및 확장성**

- **비동기 처리**: CompletableFuture를 활용한 논블로킹 비동기 처리
- **Redis 캐싱**: 반복적인 문서 분석 요청에 대한 응답 시간 단축
- **스레드 풀 최적화**: OCR과 얼굴 인식용 별도 스레드 풀 구성

### 6. **실제 서비스 환경 고려**

- **MinIO 객체 스토리지**: 클라우드 네이티브한 파일 저장 방식
- **Oracle DB**: 엔터프라이즈 환경에서 널리 사용되는 데이터베이스 지원
- **Swagger/OpenAPI**: 완전한 API 문서화로 개발자 경험 향상
- **에러 핸들링**: 예외 상황에 대한 안정적인 처리

### 7. **다양한 비교 기능**

- **얼굴 유사도 비교**: 신분증 재발급 시 본인 확인 등에 활용 가능
- **OCR 텍스트 유사도**: 계약서 비교, 영수증 중복 검사 등에 활용 가능
- **자동 비교 방식 선택**: 상황에 맞는 최적의 비교 방식 자동 선택

---

## 🛠 기술 스택

### Backend Framework

- **Java 21**: 최신 LTS 버전
- **Spring Boot 3.5.7**: 최신 Spring Boot 버전
- **Spring Web**: RESTful API 구현
- **Spring Data JPA**: 데이터 영속성 관리
- **Spring Cache**: 캐싱 기능
- **Spring AI 0.8.1**: AI 통합

### Database & Storage

- **Oracle Database**: 엔터프라이즈급 관계형 데이터베이스
- **Redis**: 캐싱 및 세션 관리
- **MinIO**: 객체 스토리지 (S3 호환)

### OCR & AI

- **Tesseract OCR (tess4j 5.8.0)**: 텍스트 추출 (한국어/영어 지원)
- **DJL (Deep Java Library)**: PyTorch 기반 얼굴 인식
- **Spring AI OpenAI**: GPT 모델을 활용한 문서 분석

### Security

- **Jasypt**: 설정 파일 암호화
- **AES-256-GCM**: 민감 정보 암호화

### Documentation

- **SpringDoc OpenAPI 2.2.0**: Swagger UI 자동 생성

### Utilities

- **Lombok**: 보일러플레이트 코드 감소
- **ImgScalr**: 이미지 리사이징
- **Jackson**: JSON 처리

---

## 🏗 아키텍처

본 프로젝트는 **헥사고날 아키텍처 (Hexagonal Architecture)**와 **DDD (Domain-Driven Design)**를 기반으로 설계되었습니다.

```
┌─────────────────────────────────────────────────────────┐
│                    Application Layer                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │   Use Cases  │  │   Services   │  │    Ports     │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
                        │
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │    Models    │  │   Services   │  │  Repositories │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
                        │
┌─────────────────────────────────────────────────────────┐
│                 Infrastructure Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ Adapters (In)│  │Adapters (Out)│  │    Config    │ │
│  │  - Web API   │  │  - OCR       │  │  - Redis     │ │
│  │  - Thymeleaf │  │  - AI        │  │  - MinIO     │ │
│  │              │  │  - Storage   │  │  - Encryption│ │
│  │              │  │  - DB        │  │              │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 레이어 설명

1. **Application Layer**: 비즈니스 유스케이스와 서비스 로직
2. **Domain Layer**: 핵심 비즈니스 모델과 도메인 서비스
3. **Infrastructure Layer**: 외부 시스템과의 통신 어댑터

이러한 구조를 통해 **비즈니스 로직과 인프라의 완전한 분리**가 가능하며, 테스트 용이성과 유지보수성이 크게 향상됩니다.

---

## 🚀 시작하기

### 사전 요구사항

다음 외부 서비스/도구가 필요합니다:

1. **Oracle Database** (예: Oracle XE)

   - JDBC URL: `jdbc:oracle:thin:@localhost:1521:XE`

2. **Redis**

   - 기본 설정: `localhost:6379`
   - 캐싱을 위해 필요

3. **MinIO**

   - 기본 설정: `http://localhost:9000`
   - 기본 자격증명: `minioadmin` / `minioadmin`
   - 버킷 이름: `ocr-study` (자동 생성)

4. **Tesseract OCR** ⚠️

   - 한국어/영어 언어 데이터 설치 필요
   - Windows: `C:\Program Files\Tesseract-OCR\tessdata`
   - macOS: `/usr/local/share/tessdata`
   - Linux: `/usr/share/tesseract-ocr/4.00/tessdata`

   > **💡 중요**: Tesseract OCR이 없어도 애플리케이션은 실행됩니다. 하지만 OCR 기능은 동작하지 않으며, 빈 텍스트를 반환합니다.
   >
   > - 이미지 업로드, 파일 저장, 얼굴 인식 등 다른 기능은 정상 동작합니다.
   > - OCR 기능을 사용하려면 반드시 Tesseract OCR을 설치해야 합니다.

#### Tesseract OCR 설치 방법

##### Windows

1. **Tesseract OCR 설치**

   - **다운로드 링크**: [UB-Mannheim Tesseract 설치 파일](https://github.com/UB-Mannheim/tesseract/wiki) 또는 [직접 다운로드](https://digi.bib.uni-mannheim.de/tesseract/)
   - **권장 버전**: 최신 버전 (5.x.x 이상)
   - 설치 프로그램 실행:
     - **중요**: 설치 중 **"Additional language data (download)"** 옵션에서 **Korean (kor)** 체크
     - 기본 설치 경로: `C:\Program Files\Tesseract-OCR` (변경 가능)
   - 설치 완료 후 재시작 권장

2. **한국어 언어 데이터 확인 및 설치**

   - 설치 후 다음 경로에 `kor.traineddata` 파일이 있는지 확인:
     ```
     C:\Program Files\Tesseract-OCR\tessdata\kor.traineddata
     ```
   - **없을 경우 수동 설치**:
     1. [한국어 언어 데이터 직접 다운로드](https://github.com/tesseract-ocr/tessdata/raw/main/kor.traineddata)
     2. 다운로드한 `kor.traineddata` 파일을 다음 경로에 복사:
        ```
        C:\Program Files\Tesseract-OCR\tessdata\
        ```
     3. 파일 이름이 `kor.traineddata`인지 확인 (확장자 포함)

3. **환경 변수 설정 (선택사항)**

   - 시스템 환경 변수에 `TESSDATA_PREFIX` 추가:
     ```
     변수명: TESSDATA_PREFIX
     변수값: C:\Program Files\Tesseract-OCR\tessdata
     ```
   - 또는 PATH에 추가: `C:\Program Files\Tesseract-OCR`

4. **설치 확인**
   ```powershell
   tesseract --version
   tesseract --list-langs
   ```
   - 출력에 `kor`과 `eng`가 포함되어 있어야 합니다

##### macOS

1. **Homebrew를 사용한 설치**

   ```bash
   # Homebrew가 설치되어 있지 않은 경우
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

   # Tesseract OCR 설치
   brew install tesseract

   # 한국어 언어 데이터 설치
   brew install tesseract-lang
   ```

2. **설치 확인**

   ```bash
   tesseract --version
   tesseract --list-langs
   ```

3. **수동 설치 (Homebrew 사용 불가 시)**
   - [Tesseract OCR 다운로드](https://github.com/tesseract-ocr/tesseract/wiki)
   - 한국어 언어 데이터: `/usr/local/share/tessdata/kor.traineddata`

##### Linux (Ubuntu/Debian)

1. **APT를 사용한 설치**

   ```bash
   # 시스템 업데이트
   sudo apt-get update

   # Tesseract OCR 설치
   sudo apt-get install tesseract-ocr

   # 한국어 언어 데이터 설치
   sudo apt-get install tesseract-ocr-kor
   ```

2. **설치 확인**
   ```bash
   tesseract --version
   tesseract --list-langs
   ```

##### 설치 후 확인 사항

프로젝트 실행 전 다음을 확인하세요:

1. **Tesseract 실행 파일 경로 확인**

   ```bash
   # Windows PowerShell
   where.exe tesseract

   # macOS/Linux
   which tesseract
   ```

2. **언어 데이터 경로 확인**

   - Windows: `C:\Program Files\Tesseract-OCR\tessdata\kor.traineddata`
   - macOS: `/usr/local/share/tessdata/kor.traineddata`
   - Linux: `/usr/share/tesseract-ocr/4.00/tessdata/kor.traineddata`

3. **환경 변수 설정 (선택사항)**
   - 환경 변수 `TESSDATA_PREFIX`를 설정하면 자동으로 경로를 찾습니다
   - 설정하지 않으면 애플리케이션이 OS별 기본 경로를 자동으로 사용합니다

### 환경 변수 설정

다음 환경 변수를 설정하는 것을 권장합니다:

```bash
# OpenAI API Key (Spring AI)
OPENAI_API_KEY=your-openai-api-key

# Redis (선택사항)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# 암호화 키 (32자, AES-256-GCM)
ENCRYPTION_KEY=your-32-character-encryption-key

# Tesseract 데이터 경로 (선택사항)
TESSDATA_PREFIX=C:\Program Files\Tesseract-OCR\tessdata
```

### 빌드 및 실행

#### 1. 프로젝트 빌드

```bash
# Linux/macOS
./gradlew clean build

# Windows PowerShell
gradlew clean build
```

#### 2. 애플리케이션 실행

```bash
# Linux/macOS
./gradlew bootRun

# Windows PowerShell
gradlew bootRun
```

#### 3. 접속

- **서버**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

---

## 📚 API 문서

### 주요 API 엔드포인트

#### 1. 이미지 업로드 및 처리

```http
POST /api/images/upload
Content-Type: multipart/form-data

file: [이미지 파일]
```

**응답 예시:**

```json
{
  "success": true,
  "message": "파일 업로드 및 처리 완료",
  "data": {
    "imageId": 1,
    "filename": "receipt.jpg",
    "ocrText": "영수증\n판매일: 2024-01-15\n금액: 4,500원",
    "hasFace": false,
    "isIdCard": false,
    "extractedInfo": null
  }
}
```

#### 2. 이미지 비교

```http
# 최근 이미지와 비교
GET /api/images/{imageId}/compare/latest

# 두 이미지 직접 비교
GET /api/images/{imageId1}/compare/{imageId2}
```

#### 3. 문서 분석

```http
# 문서 분석
GET /api/analysis/{imageId}/analyze

# 문서 진위 검증
GET /api/analysis/{imageId}/verify

# 문서 요약
GET /api/analysis/{imageId}/summarize
```

#### 4. 신분증 검증

```http
GET /api/verification/{imageId}/verify
```

### 완전한 API 문서

Swagger UI를 통해 모든 API의 상세 문서를 확인할 수 있습니다:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

---

## 📁 프로젝트 구조

```
src/main/java/com/sleekydz86/ocrstudy1/
├── application/              # 애플리케이션 레이어
│   ├── port/
│   │   ├── in/              # 인바운드 포트 (Use Cases)
│   │   │   ├── DocumentAnalysisUseCase
│   │   │   ├── IdVerificationUseCase
│   │   │   ├── ImageCompareUseCase
│   │   │   └── ImageUploadUseCase
│   │   └── out/             # 아웃바운드 포트 (인프라 인터페이스)
│   │       ├── AIAnalysisPort
│   │       ├── DocumentTypeDetectionPort
│   │       ├── FaceRecognitionPort
│   │       ├── OcrServicePort
│   │       └── StoragePort
│   └── service/             # 애플리케이션 서비스
│       ├── DocumentAnalysisService
│       ├── IdVerificationService
│       ├── ImageCompareService
│       └── ImageUploadService
│
├── domain/                   # 도메인 레이어
│   ├── model/               # 도메인 모델
│   │   ├── DocumentAnalysis
│   │   ├── DocumentType
│   │   ├── FaceRecognition
│   │   ├── IdVerification
│   │   ├── Image
│   │   ├── ImageComparison
│   │   └── OcrResult
│   ├── repository/          # 리포지토리 인터페이스
│   │   └── ImageRepository
│   └── service/             # 도메인 서비스
│       └── EncryptionService
│
├── infrastructure/          # 인프라스트럭처 레이어
│   ├── adapter/
│   │   ├── in/              # 인바운드 어댑터
│   │   │   ├── web/         # REST API 컨트롤러
│   │   │   │   ├── DocumentAnalysisController
│   │   │   │   ├── IdVerificationController
│   │   │   │   └── ImageController
│   │   │   └── thymeleaf/   # 웹 뷰 컨트롤러
│   │   │       └── ImageWebController
│   │   └── out/             # 아웃바운드 어댑터
│   │       ├── ai/          # AI 어댑터
│   │       │   └── SpringAIAnalysisAdapter
│   │       ├── encryption/  # 암호화 어댑터
│   │       │   └── AesEncryptionAdapter
│   │       ├── minio/       # MinIO 스토리지 어댑터
│   │       │   └── MinIOStorageAdapter
│   │       ├── ocr/         # OCR 어댑터
│   │       │   ├── DjlFaceRecognitionAdapter
│   │       │   ├── DjlOcrAdapter
│   │       │   └── DocumentTypeDetectionAdapter
│   │       └── persistence/ # 데이터베이스 어댑터
│   │           └── JpaImageRepository
│   └── config/              # Spring 설정
│       ├── AsyncConfig
│       ├── EncryptionConfig
│       ├── OpenAPIConfig
│       ├── RedisConfig
│       └── WebConfig
│
└── global/                   # 공통 모듈
    ├── config/              # 글로벌 설정
    └── entity/              # 공통 엔티티
        └── BaseEntity
```

---

## ⚠️ 주의 사항

1. **Tesseract OCR 설치**: 한국어 및 영어 언어 데이터가 설치되어 있어야 합니다.
2. **DJL PyTorch 네이티브**: CPU용 PyTorch 네이티브 라이브러리가 포함되어 있습니다.
3. **데이터베이스 설정**: Oracle DB 접속 정보를 환경에 맞게 설정하세요.
4. **암호화 키 관리**: 민감 정보 암호화를 위한 32자 암호화 키를 안전하게 관리하세요.
5. **외부 서비스**: MinIO, Redis, Oracle DB가 실행 중이어야 합니다.

---

## 📄 라이선스

이 프로젝트는 교육 및 연구 목적으로 작성되었습니다. 내부 사용 시 관련 규정을 준수하세요.

---

## 👥 기여

프로젝트 개선을 위한 제안이나 버그 리포트는 언제든 환영합니다!

---

**OCR Study 01** - AI 기반 지능형 문서 분석 플랫폼
