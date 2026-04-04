<img width="1004" height="539" alt="image" src="https://github.com/user-attachments/assets/de8ac15a-fbe2-45af-bc3f-9ffe2124efcc" />

<br/>

<img width="1883" height="936" alt="image" src="https://github.com/user-attachments/assets/49047948-9abd-40c4-b32d-cdbb7c814f65" />

<br/>
<img width="1882" height="741" alt="image" src="https://github.com/user-attachments/assets/6747dd22-8667-42a2-b722-89826c99da02" />
<br/>
<img width="1887" height="933" alt="image" src="https://github.com/user-attachments/assets/45a4994a-83b4-4fdb-b5e3-b7fde0f6c817" />
<br/>

# Spring Monitoring

Spring Boot **Actuator**, **Micrometer**, **Spring Boot Admin**을 한 애플리케이션에 묶고, **Next.js** 관리자 UI로 요약·통계·Actuator 요약을 보여 주는 학습·데모용 모노레포입니다.

---

## 버전·스택

| 구분              | 버전·기술                                                          |
| ----------------- | ------------------------------------------------------------------ |
| 빌드              | **Gradle** (루트 `settings.gradle.kts` + `:backend` 서브프로젝트)  |
| Spring Boot       | **4.0.5**                                                          |
| Java              | **25** (toolchain)                                                 |
| Spring Boot Admin | **4.0.2** (BOM)                                                    |
| 백엔드 모듈       | `backend` (실행 클래스 `BackendApplication`)                       |
| 프론트엔드        | Next.js **15.5**, React **19**, TypeScript **5.8**, Tailwind **4** |

---

## 저장소 구성

```
monitoring_server/
├── settings.gradle.kts     # 루트 Gradle 설정
├── gradlew / gradlew.bat   # 루트에서 :backend 태스크 실행 가능
├── backend/                # Spring Boot
│   ├── build.gradle.kts
│   └── src/main/java/.../backend/
│       └── monitoring/     # 도메인·애플리케이션·어댑터·인프라
└── frontend/               # Next.js (App Router)
    ├── app/                  # 페이지: /, /statistics, /actuator
    └── lib/                  # 포트/유스케이스/HTTP·mock 어댑터·Result 등
```

## 백엔드

### 의존성 요약 (`backend/build.gradle.kts`)

- `spring-boot-starter-web`, `actuator`, `aspectj`, `jdbc`, `data-redis`
- `spring-boot-admin-starter-server` + `starter-client`
- `micrometer-registry-prometheus`, PostgreSQL 드라이버
- `springBoot { buildInfo() }` — 빌드 메타 정보 Actuator `info` 등과 연계 가능

### 포트·URL

| 용도                                 | 포트 / URL                 |
| ------------------------------------ | -------------------------- |
| 앱 HTTP (REST, Spring Boot Admin UI) | **8080**                   |
| Management (Actuator 전용)           | **8081**                   |
| PostgreSQL (설정 예시)               | **5433** (`monitoring_db`) |
| Redis (설정 예시)                    | **9379**                   |

### 애플리케이션 진입점

- 패키지: `com.sleekydz86.monitoring.backend`
- `@SpringBootApplication`, `@EnableScheduling`, `@EnableAdminServer`

### 레이어 — `monitoring` 하위

| 레이어         | 대표 패키지·요소                                                                       | 역할                                                                         |
| -------------- | -------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------- |
| Domain         | `domain.course`, `domain.port`                                                         | `CourseSection`, `CourseCatalogPort`                                         |
| Application    | `application.dto`, `application.port.in`, `application.service`, `application.support` | 유스케이스, `DashboardPayloads`, `DashboardApplicationService`, 윈도우 키 등 |
| Adapter (in)   | `adapter.in.web`, `adapter.in.actuator`                                                | `/api/admin/*`, `course-monitoring` 엔드포인트, `courseCoverage` Health      |
| Adapter (out)  | `adapter.out.course`, `adapter.out.info`                                               | `InMemoryCourseCatalogAdapter`, `MonitoringInfoContributor`                  |
| Infrastructure | `infrastructure.config`, `infrastructure.persistence`, `infrastructure.observation`    | CORS, Micrometer, JDBC 스냅샷, 호스트·인프라·스토어 관측                     |

### REST API

| 메서드 | 경로                          | 설명                        |
| ------ | ----------------------------- | --------------------------- |
| GET    | `/api/admin/overview`         | Overview 대시보드 페이로드  |
| GET    | `/api/admin/statistics`       | 통계·메트릭 요약            |
| GET    | `/api/admin/actuator-summary` | Info·엔드포인트·Health 요약 |

쿼리 파라미터로 시간 윈도우 등을 넘기는 경우는 구현체(`AdminDashboardController` 등)를 참고하세요.

### Actuator (Management **8081**)

- 노출 예: `health`, `info`, `metrics`, `prometheus`, `mappings`, `httpexchanges`, `threaddump`, `course-monitoring`
- 예: `http://localhost:8081/actuator/health`, `http://localhost:8081/actuator/course-monitoring`

### CORS

- 설정 키: `idolglow.monitoring.frontend-origin` (기본 `http://localhost:3000`)
- `/api/**` 에 대해 `GET` / `POST` 허용 (`CorsConfig`)

### 설정 파일

- 기본: `backend/src/main/resources/application.yml`
- 로컬 전용 오버라이드는 Spring Boot 관례(`application-local.yml` 등)를 사용하면 됩니다.

---

## 프론트엔드

### 라우트 (`app/`)

| 경로          | 설명                                           |
| ------------- | ---------------------------------------------- |
| `/`           | Overview (`?window=` 로 집계 윈도우 선택 가능) |
| `/statistics` | 통계 대시보드                                  |
| `/actuator`   | Actuator 요약                                  |

서버 컴포넌트에서 데이터를 가져온 뒤 `components/*-dashboard.tsx` 로 렌더합니다.

### `lib/` 구조 개요

| 영역                 | 역할                                                                                                        |
| -------------------- | ----------------------------------------------------------------------------------------------------------- |
| `lib/domain`         | read-model 타입, `Result` (`ok` / `err` / `isOk`), 에러 타입                                                |
| `lib/application`    | `MonitoringReadPort`, 유스케이스(`monitoring-queries`, `resolve-with-fallback`), `create-monitoring-client` |
| `lib/infrastructure` | `monitoring-http.adapter`(실 API), `monitoring-mock.adapter`(샘플), `monitoring-env`                        |
| `lib/api.ts`         | 페이지에서 쓰는 `getOverview` / `getStatistics` / `getActuatorSummary` 진입점                               |

Live HTTP가 실패하면 mock과 조합한 **폴백**으로 UI가 깨지지 않게 되어 있습니다.

### 환경 변수

`frontend/.env.example` 참고 후 `.env.local` 등에 복사:

```env
MONITORING_API_BASE_URL=http://localhost:8080
MONITORING_ACTUATOR_BASE_URL=http://localhost:8081/actuator
SPRING_BOOT_ADMIN_BASE_URL=http://localhost:8080
```

---

## 보안·운영 참고

- 예시 YAML의 DB·Redis 비밀번호는 **데모용**입니다. 공개·운영 환경에서는 환경 변수·시크릿으로 치환하세요.
- Actuator는 management 포트를 분리하고, 노출 엔드포인트를 환경별로 제한하는 것이 좋습니다.

---

## 바로 확인할 주소

| 설명                   | URL                                              |
| ---------------------- | ------------------------------------------------ |
| Next.js 관리 UI        | http://localhost:3000                            |
| Spring Boot Admin      | http://localhost:8080                            |
| Actuator Health        | http://localhost:8081/actuator/health            |
| Actuator Info          | http://localhost:8081/actuator/info              |
| Prometheus 형식 메트릭 | http://localhost:8081/actuator/prometheus        |
| 커스텀 엔드포인트      | http://localhost:8081/actuator/course-monitoring |
