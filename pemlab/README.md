# PEM Security Lab

Java 27 `PEMEncoder` / `PEMDecoder`(JEP 538 Preview)로 PEM을 분석·생성하는 랩입니다.

Spring Boot 4.1.1, Vue 3 + pnpm을 사용합니다.

WebFlux는 동기 암호 API 실험과 맞지 않아 Spring MVC를 사용합니다.

## 구조

```
pem/       분석·키생성 유스케이스 · 도메인 · 어댑터
global/    암호 유틸 · 설정 · 예외
frontend/  Vue UI
```

## 실행

```powershell
$env:JAVA_HOME = "C:\Users\sleek\.jdks\ms-25.0.2"
.\gradlew.bat bootRun
```

```powershell
cd frontend
pnpm install
pnpm approve-builds
pnpm dev
```

브라우저: http://localhost:5173  
API:

- `POST /api/pem/analyze`
- `POST /api/pem/generate`
