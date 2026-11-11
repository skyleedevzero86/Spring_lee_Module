# storage_migration

MinIO·S3에 있는 파일을 화면에서 올리고 찾아보고, **파일 정보와 파일 BLOB을 DB 프로시저로 저장**하는 이관 도구입니다.<br/>
등록·수정·삭제·BLOB 저장은 모두 프로시저가 처리하고, 작업할 때마다 **작업자 · 어디서 · 언제 · 무엇을** 했는지 남깁니다.

| 구분       | 기술                                                                                                     |
| ---------- | -------------------------------------------------------------------------------------------------------- |
| 백엔드     | Java 25, Spring Boot 4.1.2 (Maven), MyBatis 3.6.0, HikariCP, AWS SDK v2 2.57.12, springdoc-openapi 3.2.0 |
| 프론트엔드 | React 19.3.1, Vite 8.3.7, swagger-ui-dist 5.34.0                                                         |
| DB         | MySQL, PostgreSQL ,oracle(추후)                                                                          |
| 스토리지   | MinIO S3 API, AWS S3                                                                                     |

DB·MinIO 설치나 Dockerfile은 포함하지 않습니다.<br/> 이미 떠 있는 DB와 스토리지에 접속 정보만 넣어 연동합니다.
<br/>

## 실행

```bash
cd backend
mvn spring-boot:run
```

```bash
cd frontend
npm install
npm run dev
```

- 화면: http://localhost:5173
- Swagger 백엔드: http://localhost:8080/swagger-ui.html · OpenAPI JSON: http://localhost:8080/v3/api-docs
- Swagger 프론트: 화면의 **API 문서** 탭. Try it out 요청에는 상단 작업자 이름이 `X-Worker` 헤더로 붙습니다.

## 사용 순서

1. 상단 **작업자**에 이름을 넣습니다. 비우면 `anonymous`로 기록됩니다.
2. **DB 연결**: 종류(Oracle·MySQL·PostgreSQL), 호스트, 포트, DB 정보, 아이디, 비밀번호를 넣고 저장 → **연결 테스트** → **이 DB 사용**.
   사용을 누르면 테이블 3개와 프로시저 4개를 그 DB에 준비하고, 상단에 `DB 연결됨`이 표시됩니다.
3. **스토리지 서버**: 종류를 고르고 접속 주소, 버킷, 아이디, 비밀번호를 넣고 저장 → 버킷이 없으면 만들기 선택 가능.
4. **파일 관리**: 선택 시 바로 BLOB까지 저장, 파일명·제목·태그·설명, 상태, 파일명을 누르면 상세 정보와 내용 보기.
   **DB에 저장**을 누르면 상세 정보와 파일 자체를 BLOB으로 저장하고, 원본 크기·DB 크기·SHA-256을 비교한 결과를 보여줍니다.
5. **스토리지 탐색**: 스토리지에 실제로 있는 파일을 체크박스로 골라 **DB에 등록**하거나 **삭제**합니다.
6. **작업 로그**: 대상·작업자·대상 ID로 조회합니다.

## 패키지 구조

```
com.sleekydz86.storagemigration
├── global        설정, 오류 처리, 응답, 암호화, JDBC·MyBatis, 작업자
├── connection    DB 연결
├── storage       스토리지 서버·객체
├── file          파일 정보·BLOB 저장·검색·내용 보기
└── audit         작업 로그
```
