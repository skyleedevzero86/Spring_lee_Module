# OpsPilot

AI Incident Triage & Fault Simulation Platform.

Spring Boot 4.1.1 + Java 25 + Vue 3 + pnpm.

## 실행

### Backend

```powershell
$env:JAVA_HOME = "C:\Users\sleek\.jdks\ms-25.0.2"
cd opspilot
.\gradlew.bat bootRun
```

기본 프로필: `local`

### Frontend

```powershell
cd opspilot/frontend
pnpm install
pnpm approve-builds
pnpm dev
```

브라우저: http://localhost:5173

## API

- `POST /api/incidents/triage`
- `GET /api/incidents`
- `GET /api/stats?granularity=HOUR|DAY|WEEK&from=&to=`
- `POST /api/mock-ai/mode/{mode}`
- `POST /api/faults/*`

## 화면에서 보는 흐름

장애 입력 → Fingerprint → Cache → AI → DB → 알림

통계는 기간(24h/7d/30d) · 단위(HOUR/DAY/WEEK)로 실제 DB 집계 차트입니다.
