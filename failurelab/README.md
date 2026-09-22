# 장애 / Timeout Lab

Java 27 `StructuredTaskScope`(Preview)로 하위 작업 실패·타임아웃·취소를 관찰하는 랩입니다.

Spring Boot 4.1.1, 헥사고날·DDD, Vue 3 + pnpm을 사용합니다.

WebFlux는 `Thread.sleep` 시뮬레이션과 Scope 취소 모델과 맞지 않아 Spring MVC를 사용합니다.

## 구조

```
scenario/   시뮬레이션 유스케이스 · 도메인 · 어댑터
global/     Structured 실행기 · 예외 · 유틸 · 설정
frontend/   Vue UI (pnpm)
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
API: `POST /api/simulations/run`
