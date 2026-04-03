# Spring Monitoring (IdolGlow)

Spring Boot **Actuator**, **Micrometer**, **Spring Boot Admin**을 한 애플리케이션에 묶고, **Next.js** 관리자 UI로 요약·통계·엔드포인트를 보여 주는 학습·데모용 모노레포입니다.

## 버전·스택

| 구분              | 버전·기술                                           |
| ----------------- | --------------------------------------------------- |
| 부모 POM          | Spring Boot **4.0.3**                               |
| Java              | **25**                                              |
| Spring Boot Admin | **4.0.2** (BOM)                                     |
| 백엔드 모듈       | `monitoring-server`                                 |
| 프론트엔드        | Next.js **^15**, React **^19**, TypeScript **^5.8** |

## 저장소 구성

```
스프링모니터링/
├── pom.xml                 # 부모 POM (모듈·BOM)
├── monitoring-server/      # Spring Boot 실행 모듈
└── frontend/               # Next.js (App Router)
```

## 백엔드 (`monitoring-server`)

### 의존성 요약

- Web, Actuator, AOP(`@Timed`), JDBC + PostgreSQL, Redis, **Spring Boot Admin Server + Client**, Micrometer Prometheus registry.

### 포트·URL (`application.yml` 기준)

| 용도                                               | 포트 / URL                 |
| -------------------------------------------------- | -------------------------- |
| 애플리케이션 HTTP (REST API, Spring Boot Admin UI) | **8080**                   |
| Management (Actuator 전용)                         | **8081**                   |
| PostgreSQL (설정 예시)                             | **5433** (`monitoring_db`) |
| Redis (설정 예시)                                  | **9379**                   |

### 레이어(헥사고날)

| 레이어         | 패키지                                                          | 역할                                                                              |
| -------------- | --------------------------------------------------------------- | --------------------------------------------------------------------------------- |
| Domain         | `domain.course`, `domain.port`                                  | `CourseSection` 값 객체, `CourseCatalogPort`(아웃바운드 포트)                     |
| Application    | `application.dto`, `application.port.in`, `application.service` | `DashboardQueryUseCase`, `DashboardApplicationService`, 응답 record               |
| Adapter (in)   | `adapter.in.web`, `adapter.in.actuator`                         | REST `/api/admin/*`, 커스텀 Actuator `course-monitoring`, Health `courseCoverage` |
| Infrastructure | `infrastructure.config`                                         | CORS, Micrometer 공통 태그·`TimedAspect`, HttpExchange 인메모리 저장소            |

### REST API (프론트용)

| 메서드 | 경로                          | 설명                               |
| ------ | ----------------------------- | ---------------------------------- |
| GET    | `/api/admin/overview`         | Overview 대시보드 페이로드         |
| GET    | `/api/admin/statistics`       | 통계·메트릭 요약                   |
| GET    | `/api/admin/actuator-summary` | Info·커스텀 엔드포인트·Health 요약 |

### Actuator (Management 포트 **8081**)

- 노출 예: `health`, `info`, `metrics`, `prometheus`, `mappings`, `httpexchanges`, `threaddump`, **`course-monitoring`**
- 예시: `http://localhost:8081/actuator/health`, `http://localhost:8081/actuator/course-monitoring`

### CORS

- `idolglow.monitoring.frontend-origin`(기본 `http://localhost:3000`)에서 `/api/**` 로 `GET`/`POST` 허용.

---

## 프론트엔드 (`frontend`)

- **App Router** (`app/`): `/`, `/statistics`, `/actuator` — 서버 컴포넌트에서 데이터 조회 후 대시보드 컴포넌트 렌더.
- **`lib/`**: 도메인 read-model, `Result` 타입, `MonitoringReadPort`, live HTTP + mock 어댑터, 실패 시 샘플 폴백(`resolveWithFallback`) 정책.
- 환경 변수 예시는 `frontend/.env.example` 참고.

```env
MONITORING_API_BASE_URL=http://localhost:8080
MONITORING_ACTUATOR_BASE_URL=http://localhost:8081/actuator
SPRING_BOOT_ADMIN_BASE_URL=http://localhost:8080
```

로컬에서 `frontend/.env.local`에 복사해 사용하면 됩니다.

---

## 사전 준비 (PostgreSQL / Redis)

`application.yml`은 아래에 맞춰져 있습니다. **이 저장소 루트에는 `docker-compose` 파일이 포함되어 있지 않습니다.** PostgreSQL·Redis는 로컬 설치, 클라우드, 또는 별도 Compose/스크립트로 위 포트·DB명·계정에 맞게 띄워 주세요.

| 항목              | 예시 값                 |
| ----------------- | ----------------------- |
| PostgreSQL DB     | `monitoring_db`         |
| 사용자 / 비밀번호 | `postgres` / `postgres` |
| Redis 비밀번호    | `123456`                |

운영·공개 저장소에서는 비밀번호를 환경 변수·시크릿으로 치환하는 것을 권장합니다.

---

## 실행 방법

### 1. 백엔드

저장소 루트 `스프링모니터링`에서:

```bash
cd monitoring-server
mvn spring-boot:run
```

또는 부모에서 모듈만 지정:

```bash
mvn -pl monitoring-server spring-boot:run
```

### 2. 프론트엔드

```bash
cd frontend
npm install
# 또는: pnpm install
cp .env.example .env.local   # Windows: copy .env.example .env.local
npm run dev
# 또는: pnpm dev
```

- UI: **http://localhost:3000**

패키지 매니저는 **npm** 또는 **pnpm** 등 본인 환경에 맞게 사용하면 됩니다.

---

## 바로 확인할 주소

| 설명              | URL                                              |
| ----------------- | ------------------------------------------------ |
| Next.js 관리자    | http://localhost:3000                            |
| Spring Boot Admin | http://localhost:8080                            |
| Actuator Health   | http://localhost:8081/actuator/health            |
| Actuator Info     | http://localhost:8081/actuator/info              |
| Prometheus        | http://localhost:8081/actuator/prometheus        |
| 커스텀 엔드포인트 | http://localhost:8081/actuator/course-monitoring |

---

## 이 프로젝트에서 다루는 주제

- Actuator 의존성·노출 엔드포인트·`management.server.port` 분리
- Health / Info / Metrics / Prometheus
- 커스텀 `HealthIndicator`, 커스텀 `@Endpoint`(Read/Write)
- Counter, Gauge, Timer, `@Timed`, 공통 태그·percentile·histogram
- Spring Boot Admin: 동일 프로세스에 Server + Client 등록
- Next.js 서버 컴포넌트에서 백엔드 API 소비

---

## 참고·문서

- 백엔드 상세: `monitoring-server/BACKEND_PORTFOLIO.md`
- 프론트엔드 상세: `frontend/FRONTEND_PORTFOLIO.md`

---

## 제한 사항 (로컬 데모 기준)

- **인증 없음** — 관리 UI·Actuator는 신뢰 네트워크에서만 사용하세요.
- **`CourseCatalogPort` 구현 빈**: 도메인 포트는 정의되어 있으나, 저장소에 **인메모리/DB 등 구현 클래스가 없으면** Spring 기동 시 빈 주입에 실패할 수 있습니다. 필요 시 `infrastructure` 또는 `adapter.out`에 어댑터를 추가하세요.
- **프론트 `lib`**: HTTP 클라이언트·`load*` 유스케이스·mock 샘플 데이터 모듈이 `create-monitoring-client.ts` 등에서 import되는 경우, 해당 파일이 저장소에 포함되어 있는지 빌드(`npm run build` / `pnpm run typecheck`)로 확인하세요.

---

## Java 버전

- **25** (`pom.xml`의 `java.version`) 기준입니다. JDK를 맞춰 설치해 주세요.
