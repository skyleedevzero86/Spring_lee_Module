# OCR Study 프로젝트

> **AI 기반 문서 처리 및 OCR 분석 프로젝트 모음**

이 저장소는 OCR(광학 문자 인식) 및 문서 분석 기능을 구현한 여러 프로젝트를 포함합니다. 각 프로젝트는 서로 다른 아키텍처와 기술 스택을 사용하여 문서 처리 워크플로우를 구현합니다.

---

## 📁 프로젝트 구조

```
OCR_STUDY/
├── ocrstudy1/              # OCR Study 01 - Spring Boot 통합 OCR 플랫폼
│   ├── README.md          # 상세 문서
│   └── ...
│
│
└── ocrstudy2/            # Python OCR 연동
│       ├── README.md      # 상세 문서
│       ├── START_GUIDE.md # 실행 가이드
│       ├── DEPENDENCY_USAGE.md
│       ├── backend-spring/
│       └── ocr-python/
```

---

## 🎯 프로젝트 개요

### OCR Study 01

**엔터프라이즈급 통합 OCR 플랫폼**

- **기술 스택**: Spring Boot 3.5.7, Java 21, Tesseract OCR, DJL, Spring AI
- **아키텍처**: 헥사고날 아키텍처 (Hexagonal Architecture) + DDD
- **주요 기능**:
  - 이미지 업로드 및 OCR 텍스트 추출 (Tesseract)
  - 얼굴 인식 및 비교 (DJL + PyTorch)
  - 문서 타입 자동 감지 (8가지 문서 타입)
  - 신분증 검증 및 정보 추출
  - AI 기반 문서 분석 (Spring AI + OpenAI)
  - 민감 정보 암호화 (AES-256-GCM)
  - Redis 캐싱을 통한 성능 최적화

**상세 문서**: [ocrstudy1/README.md](ocrstudy1/README.md)

---

### OCR Study 02

**Spring + Python OCR 연동 프로젝트**

- **기술 스택**: Spring Boot 3.2.0, Java 17, Python FastAPI, PaddleOCR
- **아키텍처**: 헥사고날 아키텍처 (Hexagonal Architecture)
- **주요 기능**:
  - Spring Boot 백엔드와 Python FastAPI OCR 서버 분리
  - REST API를 통한 마이크로서비스 아키텍처
  - PaddleOCR을 활용한 고품질 OCR 처리
  - 비동기 처리 및 WebClient 활용
  - MinIO 객체 스토리지 연동

**상세 문서**: [ocrstudy2/README.md](ocrstudy2/README.md)

---

## 🚀 빠른 시작

### OCR Study 01 실행

```bash
cd ocrstudy1

# Gradle 빌드
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun
```

**접속**: http://localhost:8080

**필수 요구사항**:

- Oracle Database
- Redis
- MinIO
- Tesseract OCR (한국어/영어 언어 데이터)

상세한 설정 방법은 [ocrstudy1/README.md](ocrstudy1/README.md)를 참고하세요.

---

### OCR Study 02 실행

#### 1단계: Python OCR 서버 실행

```bash
cd mdlist/ocr2/ocr-python

# 가상환경 생성 및 활성화
python -m venv venv
# Windows
venv\Scripts\activate
# Linux/Mac
source venv/bin/activate

# 의존성 설치
pip install -r requirements.txt

# 서버 실행
python app.py
```

✅ 확인: http://localhost:8000/health

#### 2단계: Spring 백엔드 실행

```bash
cd mdlist/ocr2/backend-spring

# Gradle 빌드 및 실행
./gradlew bootRun
```

✅ 확인: http://localhost:8080

상세한 실행 방법은 [mdlist/ocr2/START_GUIDE.md](mdlist/ocr2/START_GUIDE.md)를 참고하세요.

---

## 📊 프로젝트 비교

| 항목             | OCR Study 01       | OCR Study 02          |
| ---------------- | ------------------ | --------------------- |
| **OCR 엔진**     | Tesseract OCR      | PaddleOCR             |
| **아키텍처**     | 모놀리식 (통합)    | 마이크로서비스 (분리) |
| **언어**         | Java 21            | Java 17 + Python 3.9+ |
| **AI 분석**      | Spring AI + OpenAI | -                     |
| **얼굴 인식**    | DJL + PyTorch      | -                     |
| **암호화**       | AES-256-GCM        | -                     |
| **캐싱**         | Redis              | Redis                 |
| **스토리지**     | MinIO              | MinIO                 |
| **데이터베이스** | Oracle             | Oracle                |

---

## 🛠 기술 스택 비교

### OCR Study 01

- **Backend**: Spring Boot 3.5.7, Java 21
- **OCR**: Tesseract OCR (tess4j 5.8.0)
- **AI**: Spring AI 1.0.0-M6, OpenAI
- **얼굴 인식**: DJL 0.33.0, PyTorch
- **암호화**: AES-256-GCM
- **캐싱**: Spring Cache + Redis
- **스토리지**: MinIO
- **DB**: Oracle

### OCR Study 02

- **Backend**: Spring Boot 3.2.0, Java 17
- **OCR**: Python FastAPI + PaddleOCR
- **HTTP Client**: Spring WebFlux (WebClient)
- **캐싱**: Spring Cache + Redis
- **스토리지**: MinIO
- **DB**: Oracle

---

## 📚 주요 기능

### 공통 기능

- ✅ 이미지 업로드 및 저장 (MinIO)
- ✅ OCR 텍스트 추출
- ✅ 문서 타입 자동 감지
- ✅ 이미지 목록 조회 및 페이징
- ✅ REST API 제공
- ✅ Swagger/OpenAPI 문서화
- ✅ Thymeleaf 웹 UI

### OCR Study 01 전용

- ✅ 얼굴 인식 및 비교
- ✅ 신분증 검증 및 정보 추출
- ✅ AI 기반 문서 분석
- ✅ 문서 진위 검증
- ✅ 문서 요약 생성
- ✅ 민감 정보 암호화
- ✅ 이미지 간 유사도 비교

### OCR Study 02 전용

- ✅ 마이크로서비스 아키텍처
- ✅ Python FastAPI 독립 서버
- ✅ PaddleOCR 고품질 OCR 처리
- ✅ 비동기 HTTP 통신 (WebClient)

---

## 🔧 사전 요구사항

### 공통

- **Java**: JDK 17 이상 (OCR Study 01은 JDK 21)
- **Gradle**: 프로젝트에 포함된 Gradle Wrapper 사용
- **Oracle Database**: Oracle XE 또는 상위 버전
- **Redis**: 캐싱을 위한 Redis 서버
- **MinIO**: 객체 스토리지 서버

### OCR Study 01 추가

- **Tesseract OCR**: 한국어/영어 언어 데이터 포함 설치
- **OpenAI API Key**: AI 분석 기능 사용 시

### OCR Study 02 추가

- **Python 3.9+**: Python OCR 서버 실행용
- **pip**: Python 패키지 관리

---

## 📖 상세 문서

### OCR Study 01

- [README.md](ocrstudy1/README.md) - 전체 프로젝트 문서
- 프로젝트 구조, 아키텍처, API 문서, 설치 가이드 포함

### OCR Study 02

- [README.md](mdlist/ocr2/README.md) - 프로젝트 개요
- [START_GUIDE.md](mdlist/ocr2/START_GUIDE.md) - 상세 실행 가이드
- [DEPENDENCY_USAGE.md](mdlist/ocr2/DEPENDENCY_USAGE.md) - 의존성 사용 현황

---

## 🎓 학습 목적

이 프로젝트들은 다음을 학습하기 위한 목적으로 작성되었습니다:

1. **헥사고날 아키텍처 (Hexagonal Architecture)**

   - 포트와 어댑터 패턴
   - 비즈니스 로직과 인프라 분리

2. **DDD (Domain-Driven Design)**

   - 도메인 중심 설계
   - 도메인 모델과 리포지토리 패턴

3. **OCR 기술**

   - Tesseract OCR 사용법
   - PaddleOCR 사용법
   - OCR 결과 처리 및 분석

4. **마이크로서비스 아키텍처**

   - Spring Boot와 Python FastAPI 연동
   - REST API 통신 패턴

5. **보안 및 성능**
   - 민감 정보 암호화
   - Redis 캐싱 전략
   - 비동기 처리

---

## 📝 라이선스

이 프로젝트들은 교육 및 연구 목적으로 작성되었습니다.

---

## 👥 기여

프로젝트 개선을 위한 제안이나 버그 리포트는 언제든 환영합니다!

---

**OCR Study 프로젝트** - AI 기반 문서 처리 및 OCR 분석 플랫폼
