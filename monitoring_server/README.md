

<img width="1004" height="539" alt="image" src="https://github.com/user-attachments/assets/de8ac15a-fbe2-45af-bc3f-9ffe2124efcc" />

<br/>

<img width="1883" height="936" alt="image" src="https://github.com/user-attachments/assets/49047948-9abd-40c4-b32d-cdbb7c814f65" />

<br/>
<img width="1882" height="741" alt="image" src="https://github.com/user-attachments/assets/6747dd22-8667-42a2-b722-89826c99da02" />
<br/>
<img width="1887" height="933" alt="image" src="https://github.com/user-attachments/assets/45a4994a-83b4-4fdb-b5e3-b7fde0f6c817" />
<br/>


# Spring Monitoring 

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
monitoring_server/
├── backend/      # Spring Boot 실행 모듈
└── frontend/               # Next.js (App Router)
```

## 백엔드 

### 의존성 요약

- Web, Actuator, AOP, JDBC + PostgreSQL, Redis, **Spring Boot Admin Server + Client**, Micrometer Prometheus registry.

### 포트·URL 

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

## 프론트엔드

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

## 바로 확인할 주소

| 설명              | URL                                              |
| ----------------- | ------------------------------------------------ |
| Next.js 관리자    | http://localhost:3000                            |
| Spring Boot Admin | http://localhost:8080                            |
| Actuator Health   | http://localhost:8081/actuator/health            |
| Actuator Info     | http://localhost:8081/actuator/info              |
| Prometheus        | http://localhost:8081/actuator/prometheus        |
| 커스텀 엔드포인트 | http://localhost:8081/actuator/course-monitoring |
