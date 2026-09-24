# JVM Performance Lab

힙·G1 GC·스레드를 관찰하고 100MB 할당 전후를 비교하는 랩입니다.

Spring Boot 4.1.1, Java 27, Vue 3 + TypeScript + pnpm을 사용합니다.

WebFlux는 MXBean 동기 조회와 맞지 않아 Spring MVC를 사용합니다.

## 구조

```
monitor/   스냅샷·메모리 부하 유스케이스 · 도메인 · 어댑터
global/    포맷터 · 부하 홀더 · 설정 · 예외
frontend/  Vue + TypeScript
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

- `GET /api/jvm/snapshot`
- `POST /api/jvm/load`
