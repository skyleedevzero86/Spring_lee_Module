# Lazy Constant Lab

Java 27 무거운 AI 모델 설정을 지연 초기화하는 랩입니다.

Spring Boot 4.1.1, 헥사고날·DDD, Vue 3 + pnpm을 사용합니다.

WebFlux는 동기 실험과 맞지 않아 Spring MVC를 사용합니다.

Java 27에서는 `isInitialized`가 제거되어, 화면 상태는 애플리케이션 측에서 추적합니다.

## 구조

```
modelconfig/  설정 조회 유스케이스 · 도메인 · 어댑터
global/       LazyConstant 게이트웨이 · 설정 · 예외 · 유틸
frontend/     Vue UI
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

- `GET /api/lazy-config/status`
- `POST /api/lazy-config/use`
