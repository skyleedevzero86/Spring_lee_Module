# AI Search - RAG & 인터넷 검색 강화 스트리밍 대화 시스템
<img width="830" height="843" alt="image" src="https://github.com/user-attachments/assets/ec6c8790-79b6-4a0f-9a9b-22a15a96a1c5" />
<img width="843" height="872" alt="image" src="https://github.com/user-attachments/assets/b87490b7-994c-4de5-93e0-92aa851722ba" />

#포스팅

<a href="https://velog.io/@sleekydevzero86/spring-ai-searxng-integration">인터넷 검색 기능을 통합한 지능형 대화 Spring AI 구축하기</a>

## 📋 프로젝트 개요

AI Search는 Spring Boot 기반의 지능형 검색 및 대화 시스템입니다. RAG(Retrieval-Augmented Generation) 기술과 인터넷 검색을 결합하여 사용자에게 정확하고 최신 정보를 제공하는 스트리밍 채팅 서비스를 제공합니다.

## ✨ 주요 기능

### 🤖 다중 모드 대화

- **직접 대화 모드**: OpenAI GPT 모델과의 일반적인 대화
- **지식 기반 모드**: 업로드된 문서를 기반으로 한 RAG 대화
- **인터넷 검색 모드**: 실시간 웹 검색 결과를 기반으로 한 답변

### 📄 문서 관리

- 다양한 형식 지원: TXT, PDF, MD, DOCX
- 문서 업로드 및 벡터 검색
- 지식 베이스 구축

### 🔍 실시간 검색

- SearXNG 기반 웹 검색
- 검색 결과 스코어링 및 정렬
- 최신 정보 제공

### 💬 실시간 스트리밍

- Server-Sent Events (SSE) 기반 스트리밍
- 실시간 AI 응답 생성
- 연결 상태 관리

## 🏗️ 기술 스택

### Backend

- **Spring Boot 3.5.4** - 메인 프레임워크
- **Spring AI 1.0.1** - OpenAI 통합
- **Java 21** - 프로그래밍 언어
- **OkHttp 4.12.0** - HTTP 클라이언트
- **Hutool JSON** - JSON 처리
- **Lombok** - 코드 간소화

### Frontend

- **Thymeleaf** - 서버 사이드 템플릿
- **Vanilla JavaScript** - 클라이언트 사이드 로직
- **CSS3** - 스타일링

### 외부 서비스

- **OpenAI GPT** - AI 모델
- **SearXNG** - 웹 검색 엔진

## 📁 프로젝트 구조

```
src/main/java/com/sleekydz86/searchai/
├── controller/           # REST API 컨트롤러
│   ├── ChatController.java      # 채팅 API
│   ├── FileUploadController.java # 파일 업로드 API
│   ├── MainController.java      # 메인 페이지
│   └── SSEController.java      # SSE 연결 관리
├── global/              # 전역 설정 및 공통 클래스
│   ├── beans/          # 데이터 모델
│   │   ├── ChatEntity.java
│   │   ├── Document.java
│   │   ├── SearchResult.java
│   │   └── SearXNGResponse.java
│   ├── config/         # 설정 클래스
│   │   └── OkHttpConfig.java
│   └── enums/          # 열거형
│       ├── ChatMode.java
│       └── SSEMsgType.java
├── service/            # 비즈니스 로직
│   ├── impl/          # 서비스 구현체
│   │   ├── ChatServiceImpl.java
│   │   ├── DocumentServiceImpl.java
│   │   ├── SearXngServiceImpl.java
│   │   └── SSEServer.java
│   ├── ChatService.java
│   ├── DocumentService.java
│   └── SearXngService.java
└── resources/
    ├── templates/      # HTML 템플릿
    │   └── index.html
    └── application.yml # 설정 파일
```

## 🚀 시작하기

### 사전 요구사항

- Java 21 이상
- Maven 3.6 이상
- Docker (SearXNG 실행용)

### 1. 프로젝트 클론

```bash
git clone [repository-url]
cd Ai_Search
```

### 2. SearXNG 실행

```bash
docker run -p 6080:8080 --name searxng -d --restart=always \
  -v "D:\devolop\SearXNG:/etc/searxng" \
  -e "BASE_URL=http://localhost:6080/" \
  -e "INSTANCE_NAME=lee-instance" \
  searxng/searxng:latest
```

### 3. 설정 파일 구성

`src/main/resources/application.yml`에서 OpenAI API 키를 설정하세요:

```yaml
spring:
  ai:
    openai:
      api-key: your-openai-api-key-here
      chat:
        api-key: your-openai-api-key-here
```

### 4. 애플리케이션 실행

```bash
mvn spring-boot:run
```

### 5. 접속

브라우저에서 `http://localhost:8080`으로 접속

## 📖 API 문서

### 채팅 API

- `POST /chat/send` - 채팅 메시지 전송
- `GET /chat/health` - 서비스 상태 확인

### 파일 업로드 API

- `POST /rag/upload` - 문서 업로드
- `GET /rag/status` - 업로드 서비스 상태

### SSE API

- `GET /sse/connect?userId={userId}` - SSE 연결
- `GET /sse/status` - 활성 연결 수 확인

## 🔧 설정

### application.yml 주요 설정

```yaml
internet:
  websearch:
    url: http://localhost:6080/search # SearXNG URL
    counts: 25 # 검색 결과 수
```

### SearXNG 설정

`D:\devolop\SearXNG\settings.yml`에서 검색 엔진 설정을 조정할 수 있습니다.

## 💡 사용법

### 1. 대화 모드 선택

- **직접 대화**: AI와 자유롭게 대화
- **지식 기반**: 문서 업로드 후 질문
- **인터넷 검색**: 실시간 웹 검색 기반 답변

### 2. 문서 업로드

1. 업로드 버튼 클릭
2. 지원 형식: TXT, PDF, MD, DOCX
3. 자동으로 지식 기반 모드로 전환

### 3. 채팅

- 메시지 입력 후 Enter 또는 보내기 버튼
- 실시간 스트리밍 응답
- SSE를 통한 연결 상태 관리

## 🐛 문제 해결

### SearXNG 403 오류

- User-Agent 및 Accept 헤더 추가로 해결
- Docker 컨테이너 재시작 필요할 수 있음

### SSE 연결 오류

- 브라우저 호환성 확인
- 방화벽 설정 확인

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

---

**참고**: 이 프로젝트는 교육 및 연구 목적으로 개발되었습니다. 상업적 사용 시 관련 라이선스를 확인하세요.

